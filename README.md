* WOZGui: A Java/Swing GUI base module with button panels configurable using XML files.

This module is there to provide easily reconfigurable Panels with a lot of buttons to simulate user or system actions. To get a working Wizard-Of-Oz GUI, you will need to wrap the WizardGui class either using extension or delegation to provide a communication interface to your application. The buttons are connected using simple `Listener<String>` interfaces that can be tailored to your needs.

The code assumes that you have a bunch of activities which all need different buttons, and have all different configuration files. For an example what that can look like, run the `TestGui` class you can find in the `src/test/java` directory and uses resources from `src/test/resources`

** Installation

The module needs at least Java 11 and maven.

Just clone the repository, go to the root directory and do `mvn install`, which will build the library, or run the `TestGui` class from an IDE
