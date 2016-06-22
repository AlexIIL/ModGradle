package xyz.openmodloader.gradle.tasks.idea;

import com.google.gson.Gson;
import xyz.openmodloader.gradle.tasks.download.Version;
import xyz.openmodloader.gradle.utils.FileLocations;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.tasks.TaskAction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by mark on 20/06/2016.
 */
public class GenIdeaProjectTask extends AbstractTask {


	@TaskAction
	public void genIdeaRuns() throws IOException, ParserConfigurationException, SAXException, TransformerException {
		File file = new File(getProject().getName() + ".iml");
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(file);

		Node component = null;
		NodeList module = doc.getElementsByTagName("module").item(0).getChildNodes();
		for (int i = 0; i < module.getLength(); i++) {
			if (module.item(i).getNodeName().equals("component")) {
				component = module.item(i);
				break;
			}
		}

		Node content = null;
		NodeList moduleList = component.getChildNodes();

		for (int i = 0; i < moduleList.getLength(); i++) {
			if (moduleList.item(i).getNodeName().equals("content")) {
				content = moduleList.item(i);
			}
		}

		Element sourceFolder = doc.createElement("sourceFolder");
		sourceFolder.setAttribute("url", "file://$MODULE_DIR$/work/src");
		sourceFolder.setAttribute("isTestSource", "false");
		content.appendChild(sourceFolder);

		Gson gson = new Gson();

		Version version = gson.fromJson(new FileReader(FileLocations.minecraftJson), Version.class);

		for (Version.Library library : version.libraries) {
			if (library.allowed() && library.getFile() != null && library.getFile().exists()) {
				Element node = doc.createElement("orderEntry");
				node.setAttribute("type", "module-library");
				Element libraryElement = doc.createElement("library");
				Element classes = doc.createElement("CLASSES");
				Element javadoc = doc.createElement("JAVADOC");
				Element sources = doc.createElement("SOURCES");
				Element root = doc.createElement("root");
				root.setAttribute("url", "jar://" + library.getFile().getAbsolutePath() + "!/");
				classes.appendChild(root);
				libraryElement.appendChild(classes);
				libraryElement.appendChild(javadoc);
				libraryElement.appendChild(sources);
				node.appendChild(libraryElement);
				component.appendChild(node);
			} else if (!library.allowed()) {
				System.out.println(library.getFile().getName() + " is not allowed");
			}
		}

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(file);
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.transform(source, result);

		GenIdeaRun ideaClient = new GenIdeaRun();
		ideaClient.mainClass = "xyz.openmodloader.client.RunOMLClient";
		ideaClient.projectName = getProject().getName();
		ideaClient.configName = "Open Mod Gradle Client";
		ideaClient.outputFile = new File(FileLocations.WORKING_DIRECTORY, "/.idea/runConfigurations/OML_Client.xml");
		ideaClient.runDir = "file://$PROJECT_DIR$/run";
		ideaClient.arguments = "-Djava.library.path=" + FileLocations.MINECRAFT_NATIVES.getAbsolutePath();
		ideaClient.genRuns();
	}

}
