#set($symbol_pound='#')
#set($symbol_dollar='$')
#set($symbol_escape='\')

package ${package};

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceMetaData(name="${artifactName}", description = "${artifactDescription}", vendor = "JULIE Lab Jena, Germany")
@TypeCapability(inputs = {}, outputs = {})
public class ${analysisEngineClassname} extends JCasAnnotator_ImplBase {

	private final static Logger log = LoggerFactory.getLogger(${analysisEngineClassname}.class);
	
	/**
	 * This method is called a single time by the framework at component
	 * creation. Here, descriptor parameters are read and initial setup is done.
	 */
	@Override
	public void initialize(final UimaContext aContext) throws ResourceInitializationException {
		// TODO
	}

	/**
	 * This method is called for each document going through the component. This
	 * is where the actual work happens.
	 */
	@Override
	public void process(final JCas aJCas) throws AnalysisEngineProcessException {
		// TODO
	}

}
