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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.model.PMMLUtil;

import com.google.common.base.Charsets;

import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStackFunction;

/**
 * Evaluate a PMML model on a set of input data
 *
 */
public class PMMLEVAL extends NamedWarpScriptFunction implements WarpScriptStackFunction {
  
  public PMMLEVAL(String name) {
    super(name);
  }
  
  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    
    Object top = stack.pop();
    
    if (!(top instanceof Map)) {
      throw new WarpScriptException(getName() + " expects a map of input data on to of the stack.");
    }
    
    Map<Object,Object> params = (Map<Object,Object>) top;
    
    top = stack.pop();
    
    if (!(top instanceof ModelEvaluator<?>)) {
      throw new WarpScriptException(getName() + " expects a PMML evaluator below the input data.");
    }
    
    ModelEvaluator<?> evaluator = (ModelEvaluator<?>) top;
    
    Map<FieldName, ?> input = paramsToPMMLInput(evaluator, params);
    
    Map<FieldName, ?> output = evaluator.evaluate(input);
        
    stack.push(PMMLOutputToMap(output));
    
    return stack;
  }
  
  private static final Map<FieldName, ?> paramsToPMMLInput(ModelEvaluator<?> evaluator, Map<Object,Object> params) {
    
    Map<FieldName, Object> output = new HashMap<FieldName, Object>();

    List<InputField> inputFields = evaluator.getInputFields();
    
    for (InputField field: inputFields) {
      Object value = params.get(field.getName().getValue());
      FieldValue pmmlValue = null == value ? null : field.prepare(value);
      output.put(field.getName(), pmmlValue);
    }

    return output;
  }
  
  private static final Map<String,Object> PMMLOutputToMap(Map<FieldName,?> input) {
    
    Map<String,Object> output = new HashMap<String,Object>();
    
    for(Entry<FieldName,?> entry: input.entrySet()) {
      output.put(entry.getKey().toString(), entry.getValue());
    }
    
    return output;
  }
}
