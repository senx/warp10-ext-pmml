package io.warp10.script.ext.pmml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jpmml.evaluator.ModelEvaluatorFactory;

import io.warp10.WarpConfig;
import io.warp10.warp.sdk.WarpScriptExtension;

public class PMMLWarpScriptExtension extends WarpScriptExtension {
  
  private static Map<String,Object> functions;

  private static File root = null;
  
  private static final String MODEL_ROOT = "pmml.modelroot";
  
  private static final ModelEvaluatorFactory mef;
  
  static {
    functions = new HashMap<String,Object>();
    
    functions.put("PMMLEVAL", new PMMLEVAL("PMMLEVAL"));
    functions.put("PMMLLOAD", new PMMLLOAD("PMMLLOAD"));
    
    Properties props = WarpConfig.getProperties();
    
    if (props.containsKey(MODEL_ROOT)) {
      File dir = new File(props.getProperty(MODEL_ROOT));
      
      if (!dir.exists() || !dir.isDirectory()) {
        throw new RuntimeException("Unable to set model root directory to '" + dir.getAbsolutePath() + "'.");
      }
      
      root = dir;
    }
    
    mef = ModelEvaluatorFactory.newInstance();    
  }
  
  @Override
  public Map<String, Object> getFunctions() {
    return functions;
  }
  
  static File getRoot() {
    return root;
  }
  
  static ModelEvaluatorFactory getModelEvaluatorFactory() {
    return mef;
  }
}
