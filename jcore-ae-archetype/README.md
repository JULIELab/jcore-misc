# JCoRe AnalysisEngine Archetype

Usage:

` mvn archetype:generate -DarchetypeArtifactId=jcore-ae-archetype -DarchetypeGroupId=de.julielab`

When using some IDE dialog (e.g. from Intellij IDEA) the following properties need to defined:

* `artifactName`: Will be entered into the `name` element of the `pom.xml` file and be used for the readme file.
* `artifactDescription`: Will be entered into the `description` element of the `pom.xml` file and be used for the readme file.
* `analysisEngineClassname`: The name of the actual UIMA annotator class.
* You might additionally want to make sure that the `package` adheres to the `de.julielab.jcore.<reader/ae/consumer>.<name>` convention.
