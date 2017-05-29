/* 
 * YOUR LICENSE HEADER GOES HERE
 */
package mypackage;

import gate.creole.PackagedController;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.AutoInstanceParam;
import gate.creole.metadata.CreoleResource;

/**
 * Example packaged controller for the PR.
 * 
 * @author Johann Petrak
 */
@CreoleResource(
        name = "MyPlugin",
        autoinstances = @AutoInstance(parameters= {
          @AutoInstanceParam(name="pipelineURL", value="application/application.xgapp"),
          @AutoInstanceParam(name="menu",value="MyPlugin")
        })
)
public class MyPipeline extends PackagedController {
  // nothing else needed!
  private static final long serialVersionUID = 4259274420203872763L;
  
}
