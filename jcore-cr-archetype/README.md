# JCoRe Collection Reader Creation Archetype

This is a Maven archetype to quickly create collection reader projects in the [jcore-base](https://github.com/JULIELab/jcore-base) repository.
To use it from the command line, type `mvn archetype:generate -DarchetypeArtifactId=jcore-projects-cr-archetype -DarchetypeGroupId=de.julielab`.

When using some IDE dialog (e.g. from Intellij IDEA) the following properties need to defined:

* `artifactName`: Will be entered into the `name` element of the `pom.xml` file and be used for the readme file.
* `artifactDescription`: Will be entered into the `description` element of the `pom.xml` file and be used for the readme file.
* `uimaReaderClassname`: The name of the actual UIMA reader class.
* You might additionally want to make sure that the `package` adheres to the `de.julielab.jcore.<reader/ae/consumer>.<name>` convention.
