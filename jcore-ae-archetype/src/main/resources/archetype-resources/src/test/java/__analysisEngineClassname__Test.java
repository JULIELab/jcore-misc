#set($symbol_pound='#')#set($symbol_dollar='$')#set($symbol_escape='\')

package ${package};

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Unit tests for ${artifactId}.
 *
 */
public class ${analysisEngineClassname}Test{
    private final static Logger log = LoggerFactory.getLogger(${analysisEngineClassname}Test.class);

    @Test
    public void testAnnotator() {
        // TODO
    }
}
