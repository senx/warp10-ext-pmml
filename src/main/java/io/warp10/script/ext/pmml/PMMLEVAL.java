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
