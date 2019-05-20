//
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
      
      if (null == root && !PMMLWarpScriptExtension.useClasspath()) {
        throw new WarpScriptException(getName() + " cannot load model from file, model root directory was not set.");
      }
      
      InputStream in = null;

      File modelfile = null;
      
      if (null != root) {
        modelfile = new File(root, modelstr.substring(1));
      
        if (!modelfile.getAbsolutePath().startsWith(root.getAbsolutePath())) {
          throw new WarpScriptException(getName() + " invalid path for model.");
        }        
      }
            
      try {
        if (null == modelfile || !modelfile.exists()) {
          if (!PMMLWarpScriptExtension.useClasspath()) {
            throw new WarpScriptException(getName() + " model could not be loaded.");
          }
          
          in = WarpConfig.class.getClassLoader().getResourceAsStream(modelstr.substring(1));
          if (null == in) {
            throw new WarpScriptException(getName() + " model could not be found in class path ");
          }
        } else {
          in = new FileInputStream(modelfile);
        }

        pmml = PMMLUtil.unmarshal(in);
        
      } catch (Exception e) {
        throw new WarpScriptException(getName() + " encountered an error while loading model.", e);
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
        throw new WarpScriptException(getName() + " encountered an error while loading model.", e);
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
