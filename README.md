# Plugin template

This is a small illustrative template project for how to get started with creating a plugin for GATE version 8.4. This should also work for earlier versions, though may need some adaptations.

The template plugin should work straight away. Just clone it from Git and compile by running `ant` in the root directory. You can then run the test application in GATE Developer, or load the plugin to include the template PR in your own application. (To load the plugin, set your plugin directory in the plugin manager in GATE Developer to point to the parent directory containing the template plugin.)

The code for the PR is contained in the file `src/main/java/mypackage/MyPluginPr.java`. Currently it performs a simple annotation counting task. You can edit this to perform your task. Documentation to support you in using the GATE API can be found here:

* [GATE User Guide](https://gate.ac.uk/sale/tao/split.html)
* [GATE Javadoc](https://gate.ac.uk/releases/latest/doc/javadoc/)
* [Example code](https://gate.ac.uk/wiki/code-repository/)

The `execute` method runs on each document. The document is bound to the variable `document`. If you want to run code at the beginning or end of the whole corpus, you may do so in the `controllerExecutionStarted` and `controllerExecutionFinished` methods. These latter two methods are part of the `ControllerAwarePR` parent class. Global variables may be used to implement functionality that spans the whole task. For example, a simple counting task is demonstrated in the example code using global variables.

`src/main/java/mypackage/MyPipeline.java` is code that makes a ready made application available in the GUI when you load the plugin. If you right click on "Applications" and select "Ready Made Applications", your application will appear there. An example application is included in the `application` directory. It is entirely optional, but nice to have in some cases.

There are several files in the root directory:
* `build.properties.template` should be edited to point to your GATE installation. You should already have set the environmental variable "GATE_HOME" to point to your GATE installation, in which case the one in this file probably won't be used, except for in certain cases.
* `build.xml` shouldn't require any editing, other than to rename your plugin.
* `creole.xml` can be edited to include the name and some information about your plugin, as well as any libraries you need to use.

