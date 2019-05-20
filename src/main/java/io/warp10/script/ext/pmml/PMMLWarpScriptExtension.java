//
//   Copyright 2019  SenX S.A.S.
//
//   This program is free software: you can redistribute it and/or modify
//   it under the terms of the GNU Affero General Public License as
//   published by the Free Software Foundation, either version 3 of the
//   License, or (at your option) any later version.
//
//   This program is distributed in the hope that it will be useful,
//   but WITHOUT ANY WARRANTY; without even the implied warranty of
//   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//   GNU Affero General Public License for more details.
//
//   You should have received a copy of the GNU Affero General Public License
//   along with this program.  If not, see <http://www.gnu.org/licenses/>.
//

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
  private static final String USE_CLASSPATH = "pmml.useclasspath";
  
  private static final ModelEvaluatorFactory mef;
  
  private static final boolean useClasspath;
  
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
  
    useClasspath = "true".equals(props.getProperty(USE_CLASSPATH));
    
    mef = ModelEvaluatorFactory.newInstance();    
  }
  
  @Override
  public Map<String, Object> getFunctions() {
    return functions;
  }
  
  static File getRoot() {
    return root;
  }
  
  static boolean useClasspath() {
    return useClasspath();
  }
  
  static ModelEvaluatorFactory getModelEvaluatorFactory() {
    return mef;
  }
}
