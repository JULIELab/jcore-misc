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
public class ${consumerClassname}Test{
    private final static Logger log = LoggerFactory.getLogger(${consumerClassname}Test.class);

    @Test
    public void testConsumer() {
        // TODO
    }
}
