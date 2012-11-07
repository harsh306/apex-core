/**
 * Copyright (c) 2012-2012 Malhar, Inc.
 * All rights reserved.
 */
package com.malhartech.stram;

import com.google.common.collect.Sets;
import com.malhartech.annotation.InputPortFieldAnnotation;
import com.malhartech.annotation.OutputPortFieldAnnotation;
import com.malhartech.api.BaseOperator;
import com.malhartech.api.Context.OperatorContext;
import com.malhartech.api.DAG;
import com.malhartech.api.DAG.OperatorWrapper;
import com.malhartech.api.DAG.StreamDecl;
import com.malhartech.api.DefaultInputPort;
import com.malhartech.api.DefaultOutputPort;
import com.malhartech.api.Operator;
import com.malhartech.dag.DefaultSerDe;
import com.malhartech.dag.GenericTestModule;
import com.malhartech.dag.TestGeneratorInputModule;
import com.malhartech.stram.cli.StramClientUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class DAGBuilderTest {

  public static OperatorWrapper assertNode(DAG dag, String id) {
      OperatorWrapper n = dag.getOperatorWrapper(id);
      assertNotNull("operator exists id=" + id, n);
      return n;
  }

  /**
   * Test read from stram-site.xml in Hadoop configuration format.
   */
  @Test
  public void testLoadFromConfigXml() {
    Configuration conf = StramClientUtils.addStramResources(new Configuration());
    //Configuration.dumpConfiguration(conf, new PrintWriter(System.out));

    DAGPropertiesBuilder builder = new DAGPropertiesBuilder();
    builder.addFromConfiguration(conf);

    DAG dag = builder.getApplication(new Configuration(false));
    dag.validate();

//    Map<String, NodeConf> moduleConfs = tb.getAllOperators();
    assertEquals("number of module confs", 6, dag.getAllOperators().size());

    OperatorWrapper module1 = assertNode(dag, "module1");
    OperatorWrapper module2 = assertNode(dag, "module2");
    OperatorWrapper module3 = assertNode(dag, "module3");
    OperatorWrapper module4 = assertNode(dag, "module4");

    assertNotNull("moduleConf for root", module1);
    assertEquals("moduleId set", "module1", module1.getId());

    // verify module instantiation
    assertEquals(module1.getOperator().getClass(), GenericTestModule.class);
    GenericTestModule GenericTestNode = (GenericTestModule)module1.getOperator();
    assertEquals("myStringPropertyValue", GenericTestNode.getMyStringProperty());

    // check links
    assertEquals("module1 inputs", 0, module1.getInputStreams().size());
    assertEquals("module1 outputs", 1, module1.getOutputStreams().size());
    StreamDecl n1n2 = module2.getInputStreams().get(module2.getInputPortMeta(((GenericTestModule)module2.getOperator()).inport1));
    assertNotNull("n1n2", n1n2);

    // output/input stream object same
    assertEquals("rootNode out is module2 in", n1n2, module1.getOutputStreams().get(module1.getOutputPortMeta(((GenericTestModule)module1.getOperator()).outport1)));
    assertEquals("n1n2 source", module1, n1n2.getSource().getOperatorWrapper());
    Assert.assertEquals("n1n2 targets", 1, n1n2.getSinks().size());
    Assert.assertEquals("n1n2 target", module2, n1n2.getSinks().get(0).getOperatorWrapper());

    assertEquals("stream name", "n1n2", n1n2.getId());
    Assert.assertFalse("n1n2 not inline (default)", n1n2.isInline());

    // module 2 streams to module 3 and module 4
    assertEquals("module 2 number of outputs", 1, module2.getOutputStreams().size());
    StreamDecl fromNode2 = module2.getOutputStreams().values().iterator().next();

    Set<OperatorWrapper> targetNodes = new HashSet<OperatorWrapper>();
    for (DAG.InputPortMeta ip : fromNode2.getSinks()) {
      targetNodes.add(ip.getOperatorWrapper());
    }
    Assert.assertEquals("outputs " + fromNode2, Sets.newHashSet(module3, module4), targetNodes);

    OperatorWrapper module6 = assertNode(dag, "module6");

    List<OperatorWrapper> rootNodes = dag.getRootOperators();
    assertEquals("number root modules", 2, rootNodes.size());
    assertTrue("root module2", rootNodes.contains(module1));
    assertTrue("root module6", rootNodes.contains(module6));

    for (OperatorWrapper n : rootNodes) {
      printTopology(n, dag, 0);
    }

  }

  public void printTopology(OperatorWrapper module, DAG tplg, int level) {
      String prefix = "";
      if (level > 0) {
        prefix = StringUtils.repeat(" ", 20*(level-1)) + "   |" + StringUtils.repeat("-", 17);
      }
      System.out.println(prefix + module.getId());
      for (StreamDecl downStream : module.getOutputStreams().values()) {
          if (!downStream.getSinks().isEmpty()) {
            for (DAG.InputPortMeta targetNode : downStream.getSinks()) {
              printTopology(targetNode.getOperatorWrapper(), tplg, level+1);
            }
          }
      }
  }

  @Test
  public void testLoadFromPropertiesFile() throws IOException {
      Properties props = new Properties();
      String resourcePath = "/testTopology.properties";
      InputStream is = this.getClass().getResourceAsStream(resourcePath);
      if (is == null) {
        fail("Could not load " + resourcePath);
      }
      props.load(is);
      DAGPropertiesBuilder pb = new DAGPropertiesBuilder()
        .addFromProperties(props);

      DAG dag = pb.getApplication(new Configuration(false));
      dag.validate();

      assertEquals("number of module confs", 5, dag.getAllOperators().size());
      assertEquals("number of root modules", 1, dag.getRootOperators().size());

      StreamDecl s1 = dag.getStream("n1n2");
      assertNotNull(s1);
      assertTrue("n1n2 inline", s1.isInline());

      OperatorWrapper module3 = dag.getOperatorWrapper("module3");
      assertEquals("module3.classname", GenericTestModule.class, module3.getOperator().getClass());

      GenericTestModule dmodule3 = (GenericTestModule)module3.getOperator();
      assertEquals("myStringProperty " + dmodule3, "myStringPropertyValueFromTemplate", dmodule3.getMyStringProperty());
      assertFalse("booleanProperty " + dmodule3, dmodule3.booleanProperty);

      OperatorWrapper module4 = dag.getOperatorWrapper("module4");
      GenericTestModule dmodule4 = (GenericTestModule)module4.getOperator();
      assertEquals("myStringProperty " + dmodule4, "overrideModule4", dmodule4.getMyStringProperty());
      assertEquals("setterOnlyModule4 " + dmodule4, "setterOnlyModule4", dmodule4.propertySetterOnly);
      assertTrue("booleanProperty " + dmodule4, dmodule4.booleanProperty);

      StreamDecl input1 = dag.getStream("inputStream");
      assertNotNull(input1);
      Assert.assertEquals("input1 source", dag.getOperatorWrapper("inputModule"), input1.getSource().getOperatorWrapper());
      Set<OperatorWrapper> targetNodes = new HashSet<OperatorWrapper>();
      for (DAG.InputPortMeta targetPort : input1.getSinks()) {
        targetNodes.add(targetPort.getOperatorWrapper());
      }

      Assert.assertEquals("input1 target ", Sets.newHashSet(dag.getOperatorWrapper("module1"), module3, module4), targetNodes);

  }

  @Test
  public void testCycleDetection() {
     DAG dag = new DAG();

     //NodeConf module1 = b.getOrAddNode("module1");
     GenericTestModule module2 = dag.addOperator("module2", GenericTestModule.class);
     GenericTestModule module3 = dag.addOperator("module3", GenericTestModule.class);
     GenericTestModule module4 = dag.addOperator("module4", GenericTestModule.class);
     //NodeConf module5 = b.getOrAddNode("module5");
     //NodeConf module6 = b.getOrAddNode("module6");
     GenericTestModule module7 = dag.addOperator("module7", GenericTestModule.class);

     // strongly connect n2-n3-n4-n2
     dag.addStream("n2n3", module2.outport1, module3.inport1);

     dag.addStream("n3n4", module3.outport1, module4.inport1);

     dag.addStream("n4n2", module4.outport1, module2.inport1);

     // self referencing module cycle
     StreamDecl n7n7 = dag.addStream("n7n7", module7.outport1, module7.inport1);
     try {
       n7n7.addSink(module7.inport1);
       fail("cannot add to stream again");
     } catch (Exception e) {
       // expected, stream can have single input/output only
     }

     List<List<String>> cycles = new ArrayList<List<String>>();
     dag.findStronglyConnected(dag.getOperatorWrapper(module7), cycles);
     assertEquals("module self reference", 1, cycles.size());
     assertEquals("module self reference", 1, cycles.get(0).size());
     assertEquals("module self reference", module7.getName(), cycles.get(0).get(0));

     // 3 module cycle
     cycles.clear();
     dag.findStronglyConnected(dag.getOperatorWrapper(module4), cycles);
     assertEquals("3 module cycle", 1, cycles.size());
     assertEquals("3 module cycle", 3, cycles.get(0).size());
     assertTrue("module2", cycles.get(0).contains(module2.getName()));
     assertTrue("module3", cycles.get(0).contains(module3.getName()));
     assertTrue("module4", cycles.get(0).contains(module4.getName()));

     try {
       dag.validate();
       fail("validation should fail");
     } catch (IllegalStateException e) {
       // expected
     }

  }

  public static class TestSerDe extends DefaultSerDe {

  }

  static class ValidationModule extends BaseOperator {
    @OutputPortFieldAnnotation(name="goodOutputPort")
    final public transient DefaultOutputPort<Object> goodOutputPort = new DefaultOutputPort<Object>(this);

    @OutputPortFieldAnnotation(name="badOutputPort")
    final public transient DefaultOutputPort<Object> badOutputPort = new DefaultOutputPort<Object>(this);
  }

  static class CounterModule extends BaseOperator {
    @InputPortFieldAnnotation(name="countInputPort")
    final public transient InputPort<Object> countInputPort = new DefaultInputPort<Object>(this) {
      @Override
      final public void process(Object payload) {
      }
    };
  }

  static class ConsoleOutputModule extends BaseOperator {
    @InputPortFieldAnnotation(name="echoInputPort")
    final public transient InputPort<Object> echoInputPort = new DefaultInputPort<Object>(this) {
      @Override
      final public void process(Object payload) {
      }
    };
  }

  @Test
  public void testJavaBuilder() throws Exception {

    DAG dag = new DAG();

    ValidationModule validationNode = dag.addOperator("validationNode", ValidationModule.class);
    CounterModule countGoodNode = dag.addOperator("countGoodNode", CounterModule.class);
    CounterModule countBadNode = dag.addOperator("countBadNode", CounterModule.class);
    //ConsoleOutputModule echoBadNode = dag.addOperator("echoBadNode", ConsoleOutputModule.class);

    // good tuples to counter module
    dag.addStream("goodTuplesStream", validationNode.goodOutputPort, countGoodNode.countInputPort);

    // bad tuples to separate stream and echo module
    // (stream with 2 outputs)
    dag.addStream("badTuplesStream", validationNode.badOutputPort, countBadNode.countInputPort);

    Assert.assertEquals("number root modules", 1, dag.getRootOperators().size());
    Assert.assertEquals("root module id", "validationNode", dag.getRootOperators().get(0).getId());

    dag.getContextAttributes(countGoodNode).attr(OperatorContext.SPIN_MILLIS).set(10);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DAG.write(dag, bos);

    System.out.println("serialized size: " + bos.toByteArray().length);

    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    DAG dagClone = DAG.read(bis);
    Assert.assertNotNull(dagClone);
    Assert.assertEquals("number modules in clone", dag.getAllOperators().size(), dagClone.getAllOperators().size());
    Assert.assertEquals("number root modules in clone", 1, dagClone.getRootOperators().size());
    Assert.assertTrue("root module in modules", dagClone.getAllOperators().contains(dagClone.getRootOperators().get(0)));


    Operator countGoodNodeClone = dagClone.getOperatorWrapper("countGoodNode").getOperator();
    Assert.assertEquals("", new Integer(10), dagClone.getContextAttributes(countGoodNodeClone).attr(OperatorContext.SPIN_MILLIS).get());

  }

  static class ValidationTestOperator extends BaseOperator {
    @NotNull
    @Pattern(regexp=".*malhar.*", message="Value has to contain 'malhar'!")
    private String stringField1;

    @Min(2)
    private int intField1;

    @AssertTrue(message="stringField1 should end with intField1")
    private boolean isValidConfiguration() {
      return stringField1.endsWith(String.valueOf(intField1));
    }

    private String getterProperty2 = "";

    @NotNull
    public String getProperty2() {
      return getterProperty2;
    }

    public void setProperty2(String s) {
      // annotations need to be on the getter
      getterProperty2 = s;
    }

    public class Nested {
      @NotNull
      private String property = "";

      public String getProperty() {
        return property;
      }

      public void setProperty(String property) {
        this.property = property;
      }

    }

    @Valid
    private final Nested nestedBean = new Nested();

    private String stringProperty2;

    public String getStringProperty2() {
      return stringProperty2;
    }

    public void setStringProperty2(String stringProperty2) {
      this.stringProperty2 = stringProperty2;
    }

  }

  @Test
  public void testOperatorValidation() {

    ValidationTestOperator bean = new ValidationTestOperator();
    bean.stringField1 = "malhar1";
    bean.intField1 = 1;

    // ensure validation standalone produces expected results
    ValidatorFactory factory =
        Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    Set<ConstraintViolation<ValidationTestOperator>> constraintViolations =
             validator.validate(bean);
    //for (ConstraintViolation<ValidationTestOperator> cv : constraintViolations) {
    //  System.out.println("validation error: " + cv);
    //}
    Assert.assertEquals("" + constraintViolations,1, constraintViolations.size());
    ConstraintViolation<ValidationTestOperator> cv = constraintViolations.iterator().next();
    Assert.assertEquals("", bean.intField1, cv.getInvalidValue());
    Assert.assertEquals("", "intField1", cv.getPropertyPath().toString());

    // ensure DAG validation produces matching results
    DAG dag = new DAG();
    bean = dag.addOperator("testOperator", bean);

    try {
      dag.validate();
      Assert.fail("should throw ConstraintViolationException");
    } catch (ConstraintViolationException e) {
      Assert.assertEquals("", constraintViolations, e.getConstraintViolations());
    }

    try {
      bean.intField1 = 3;
      dag.validate();
      Assert.fail("should throw ConstraintViolationException");
    } catch (ConstraintViolationException e) {
      ConstraintViolation<?> cv2 = e.getConstraintViolations().iterator().next();
      Assert.assertEquals("" + e.getConstraintViolations(), 1, constraintViolations.size());
      Assert.assertEquals("", false, cv2.getInvalidValue());
      Assert.assertEquals("", "validConfiguration", cv2.getPropertyPath().toString());
    }
    bean.stringField1 = "malhar3";

    // annotated getter
    try {
      bean.getterProperty2 = null;
      dag.validate();
      Assert.fail("should throw ConstraintViolationException");
    } catch (ConstraintViolationException e) {
      ConstraintViolation<?> cv2 = e.getConstraintViolations().iterator().next();
      Assert.assertEquals("" + e.getConstraintViolations(), 1, constraintViolations.size());
      Assert.assertEquals("", null, cv2.getInvalidValue());
      Assert.assertEquals("", "property2", cv2.getPropertyPath().toString());
    }
    bean.getterProperty2 = "";

    // nested property
    try {
      bean.nestedBean.property = null;
      dag.validate();
      Assert.fail("should throw ConstraintViolationException");
    } catch (ConstraintViolationException e) {
      ConstraintViolation<?> cv2 = e.getConstraintViolations().iterator().next();
      Assert.assertEquals("" + e.getConstraintViolations(), 1, constraintViolations.size());
      Assert.assertEquals("", null, cv2.getInvalidValue());
      Assert.assertEquals("", "nestedBean.property", cv2.getPropertyPath().toString());
    }
    bean.nestedBean.property = "";

    // all valid
    dag.validate();

  }

  @Test
  public void testPortConnectionValidation() {

    DAG dag = new DAG();
    TestGeneratorInputModule input = dag.addOperator("input1", TestGeneratorInputModule.class);

    try {
    dag.validate();
    Assert.fail("should raise port not connected for o1.input1");
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("", "Output port connection required: input1.outputPort", e.getMessage());
    }

    GenericTestModule o1 = dag.addOperator("o1", GenericTestModule.class);
    dag.addStream("stream1", input.outport, o1.inport1);
    dag.validate();

  }

  @Test
  public void testOperatorConfigurationLookup() {

    Properties props = new Properties();

    // match operator by name
    props.put("stram.template.matchId1.matchIdRegExp", ".*operator1.*");
    props.put("stram.template.matchId1.stringProperty2", "stringProperty2Value-matchId1");
    props.put("stram.template.matchId1.nested.property", "nested.propertyValue-matchId1");

    // match class name, lower priority
    props.put("stram.template.matchClass1.matchClassNameRegExp", ".*" + ValidationTestOperator.class.getSimpleName());
    props.put("stram.template.matchClass1.stringProperty2", "stringProperty2Value-matchClass1");

    // match class name
    props.put("stram.template.t2.matchClassNameRegExp", ".*"+GenericTestModule.class.getSimpleName());
    props.put("stram.template.t2.myStringProperty", "myStringPropertyValue");

    // direct setting
    props.put("stram.operator.operator3.emitFormat", "emitFormatValue");

    DAG dag = new DAG();
    Operator operator1 = dag.addOperator("operator1", new ValidationTestOperator());
    Operator operator2 = dag.addOperator("operator2", new ValidationTestOperator());
    Operator operator3 = dag.addOperator("operator3", new GenericTestModule());

    DAGPropertiesBuilder pb = new DAGPropertiesBuilder();
    pb.addFromProperties(props);

    Map<String, String> configProps = pb.getProperties(dag.getOperatorWrapper(operator1), "appName");
    Assert.assertEquals("" + configProps, 2, configProps.size());
    Assert.assertEquals("" + configProps, "stringProperty2Value-matchId1", configProps.get("stringProperty2"));
    Assert.assertEquals("" + configProps, "nested.propertyValue-matchId1", configProps.get("nested.property"));

    configProps = pb.getProperties(dag.getOperatorWrapper(operator2), "appName");
    Assert.assertEquals("" + configProps, 1, configProps.size());
    Assert.assertEquals("" + configProps, "stringProperty2Value-matchClass1", configProps.get("stringProperty2"));

    configProps = pb.getProperties(dag.getOperatorWrapper(operator3), "appName");
    Assert.assertEquals("" + configProps, 2, configProps.size());
    Assert.assertEquals("" + configProps, "myStringPropertyValue", configProps.get("myStringProperty"));
    Assert.assertEquals("" + configProps, "emitFormatValue", configProps.get("emitFormat"));

  }

  @Test
  public void testSetOperatorProperties() {

    Configuration conf = new Configuration(false);
    conf.set("stram.operator.o1.myStringProperty", "myStringPropertyValue");

    DAG dag = new DAG();
    GenericTestModule o1 = dag.addOperator("o1", new GenericTestModule());

    DAGPropertiesBuilder pb = new DAGPropertiesBuilder();
    pb.addFromConfiguration(conf);

    pb.setOperatorProperties(dag, "testSetOperatorProperties");
    Assert.assertEquals("o1.myStringProperty", "myStringPropertyValue", o1.getMyStringProperty());

  }

}
