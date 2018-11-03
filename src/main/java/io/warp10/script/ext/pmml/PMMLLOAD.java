//
//   Copyright 2018  SenX S.A.S.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package io.warp10.script.ext.pmml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.dmg.pmml.PMML;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.model.PMMLUtil;

import com.google.common.base.Charsets;

import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStackFunction;

/**
 * Load a PMML model
 *
 */
public class PMMLLOAD extends NamedWarpScriptFunction implements WarpScriptStackFunction {
  
  public PMMLLOAD(String name) {
    super(name);
  }
  
  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    
    Object top = stack.pop();
    
    if (!(top instanceof String)) {
      throw new WarpScriptException(getName() + " expects a PMML model or reference to a PMML file on top of the stack.");
    }
    
    String modelstr = top.toString();
    
    PMML pmml = null;
    
    if (modelstr.startsWith("@")) {
      File root = PMMLWarpScriptExtension.getRoot();
      
      if (null == root) {
        throw new WarpScriptException(getName() + " cannot load model from file, model root directory was not set.");
      }
      
      File modelfile = new File(root, modelstr.substring(1));
      
      if (!modelfile.getAbsolutePath().startsWith(root.getAbsolutePath())) {
        throw new WarpScriptException(getName() + " invalid path for model.");
      }
      
      InputStream in = null;
      
      try {
        in = new FileInputStream(modelfile);
      
        pmml = PMMLUtil.unmarshal(in);
        
      } catch (Exception e) {
        throw new WarpScriptException(getName() + " encountered an error while loading the model.", e);
      } finally {
        if (null != in) {
          try { in.close(); } catch (Exception e) {}
        }
      }
    } else {
      InputStream in = new ByteArrayInputStream(modelstr.getBytes(Charsets.UTF_8));
      
      try {
        pmml = PMMLUtil.unmarshal(in);        
      } catch (Exception e) {
        throw new WarpScriptException(getName() + " encountered an error while loading the model.", e);
      } finally {
        if (null != in) {
          try { in.close(); } catch (Exception e) {}
        }
      }
    }
    
    ModelEvaluator<?> evaluator = PMMLWarpScriptExtension.getModelEvaluatorFactory().newModelEvaluator(pmml);

    stack.push(evaluator);
    
    return stack;
  }
}
