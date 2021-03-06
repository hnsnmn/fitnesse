package fitnesse.testrunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fitnesse.testsystems.Descriptor;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemFactory;
import fitnesse.testsystems.fit.CommandRunningFitClient;
import fitnesse.testsystems.fit.FitClient;
import fitnesse.testsystems.fit.FitClientBuilder;
import fitnesse.testsystems.fit.FitTestSystem;
import fitnesse.testsystems.fit.InProcessFitClientBuilder;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.HtmlSlimTestSystem;
import fitnesse.testsystems.slim.InProcessSlimClientBuilder;
import fitnesse.testsystems.slim.SlimClientBuilder;
import fitnesse.testsystems.slim.SlimCommandRunningClient;
import fitnesse.testsystems.slim.tables.SlimTableFactory;

public class MultipleTestSystemFactory implements TestSystemFactory, TestSystemFactoryRegistrar {
  private final Map<String, TestSystemFactory> testSystemFactories = new HashMap<String, TestSystemFactory>(4);

  public MultipleTestSystemFactory(SlimTableFactory slimTableFactory, CustomComparatorRegistry customComparatorRegistry) {
    registerTestSystemFactory("slim", new HtmlSlimTestSystemFactory(slimTableFactory, customComparatorRegistry));
    registerTestSystemFactory("slim^inprocess", new InProcessHtmlSlimTestSystemFactory(slimTableFactory, customComparatorRegistry));
    registerTestSystemFactory("fit", new FitTestSystemFactory());
    registerTestSystemFactory("fit^inprocess", new InProcessFitTestSystemFactory());
  }

  @Override
  public void registerTestSystemFactory(String name, TestSystemFactory testSystemFactory) {
    testSystemFactories.put(name, testSystemFactory);
  }

  public TestSystem create(Descriptor descriptor) throws IOException {
    TestSystemFactory factory = testSystemFactories.get(descriptor.getTestSystemType().toLowerCase());
    TestSystem testSystem = factory.create(descriptor);
    return testSystem;
  }

  static class HtmlSlimTestSystemFactory implements TestSystemFactory {
    private final SlimTableFactory slimTableFactory;
    private final CustomComparatorRegistry customComparatorRegistry;

    public HtmlSlimTestSystemFactory(SlimTableFactory slimTableFactory,
                                     CustomComparatorRegistry customComparatorRegistry) {
      this.slimTableFactory = slimTableFactory;
      this.customComparatorRegistry = customComparatorRegistry;
    }

    public final TestSystem create(Descriptor descriptor) throws IOException {
      SlimCommandRunningClient slimClient = new SlimClientBuilder(descriptor).build();
      HtmlSlimTestSystem testSystem = new HtmlSlimTestSystem(descriptor.getTestSystemName(), slimClient,
              slimTableFactory, customComparatorRegistry);

      return testSystem;
    }
  }

  static class InProcessHtmlSlimTestSystemFactory implements TestSystemFactory {
    private final SlimTableFactory slimTableFactory;
    private final CustomComparatorRegistry customComparatorRegistry;

    public InProcessHtmlSlimTestSystemFactory(SlimTableFactory slimTableFactory,
                                              CustomComparatorRegistry customComparatorRegistry) {
      this.slimTableFactory = slimTableFactory;
      this.customComparatorRegistry = customComparatorRegistry;
    }

    public TestSystem create(Descriptor descriptor) throws IOException {
      SlimCommandRunningClient slimClient = new InProcessSlimClientBuilder(descriptor).build();
      HtmlSlimTestSystem testSystem = new HtmlSlimTestSystem(descriptor.getTestSystemName(), slimClient,
              slimTableFactory, customComparatorRegistry);

      return testSystem;
    }
  }

  static class FitTestSystemFactory implements TestSystemFactory {

    public FitTestSystem create(Descriptor descriptor) throws IOException {
      CommandRunningFitClient fitClient = new FitClientBuilder(descriptor).build();

      return new FitTestSystem(descriptor.getTestSystemName(), fitClient);
    }
  }

  static class InProcessFitTestSystemFactory implements TestSystemFactory {

    public FitTestSystem create(Descriptor descriptor) throws IOException {
      CommandRunningFitClient fitClient = new InProcessFitClientBuilder(descriptor).build();

      return new FitTestSystem(descriptor.getTestSystemName(), fitClient);
    }
  }
}