package de.julielab.jcore.management;

import com.ximpleware.AutoPilot
import com.ximpleware.VTDGen
import com.ximpleware.VTDNav
import com.ximpleware.XMLModifier
import de.julielab.utilities.aether.MavenProjectUtilities
import de.julielab.xml.JulieXMLConstants
import de.julielab.xml.JulieXMLTools
import java.io.File
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    if (args.size != 2) {
        println("Usage: VersionNormalizer <pom file> <new version>")
        exitProcess(1)
    }

    val version: String = args[1]
    val file = File(args[0])
    val mavenProperties = MavenProjectUtilities.getEffectivePomModel(file).properties
    val artifactIdProperties2Version = mavenProperties.propertyNames().asSequence().map { o -> Pair((o as String).replace("-version", ""), "\${$o}" ) }.toMap()
    val projectModules = MavenProjectUtilities.getProjectModules(file, true)
    val artifactIds: Set<String> = projectModules.map { m -> File(Paths.get(m, "pom.xml").toString()) }.map { f -> MavenProjectUtilities.getRawPomModel(f) }.map { m -> m.artifactId }.toSet()
    for (module in projectModules) {
        val vg = VTDGen()
        val pomPath = Paths.get(module, "pom.xml").toString()
        vg.parseFile(pomPath, false)
        var nav = vg.nav
        var ap = AutoPilot(nav)
        ap.selectXPath("/project/parent/version")
        var xm = XMLModifier(nav)

        var i = ap.evalXPath()
        if (i != -1) {
            var currentIndex = nav.currentIndex
            while (nav.getTokenType(currentIndex) != VTDNav.TOKEN_CHARACTER_DATA)
                ++currentIndex
            xm.updateToken(currentIndex, version)
        }
        ap.resetXPath()
        ap.selectXPath("/project/version")
        i = ap.evalXPath()
        if (i != -1) {
            xm.remove()
        }


        val field1 = JulieXMLTools.createField(JulieXMLConstants.NAME, "artifactId", JulieXMLConstants.XPATH, "artifactId")
        val field2 = JulieXMLTools.createField(JulieXMLConstants.NAME, "version", JulieXMLConstants.XPATH, "version")
        val fields = listOf(field1, field2)
        val rowIt = JulieXMLTools.constructRowIterator(nav, "/project/dependencies/dependency[groupId='de.julielab']", fields, pomPath)
        for (row in rowIt) {
            if (row["version"] == null)
                continue
            if (artifactIds.contains(row.get("artifactId"))) {
                val artifactId = row["artifactId"]
                val versionExpression = if (!artifactIdProperties2Version.containsKey(artifactId)) version else artifactIdProperties2Version[artifactId]
                val vtdIndex: Int = row[JulieXMLConstants.VTD_INDEX] as Int
                nav.push()
                nav.recoverNode(vtdIndex)
                // navigate to the version element
                JulieXMLTools.setElementText(nav, AutoPilot(nav), xm, "version", versionExpression)
                nav.pop()
            }
        }


        val outputFile = Paths.get(module, "pom.xml")
        xm.output(outputFile.toString())
    }
}

