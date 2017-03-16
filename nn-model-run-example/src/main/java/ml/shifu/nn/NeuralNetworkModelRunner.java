/*
 * Copyright [2013-2017] PayPal Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ml.shifu.nn;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.sax.SAXSource;

import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.EvaluatorUtil;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.NeuralNetworkEvaluator;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.xml.sax.InputSource;

/**
 * TODO
 * 
 * @author Zhang David (pengzhang@paypal.com)
 */
public class NeuralNetworkModelRunner {

    private final List<NeuralNetworkEvaluator> evaluators;

    List<FieldName> activeFields;

    private Set<String> activeNames;

    private static PMML loadPMML(File file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            InputSource source = new InputSource(is);
            SAXSource transformedSource = ImportFilter.apply(source);
            return JAXBUtil.unmarshalPMML(transformedSource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public NeuralNetworkModelRunner(String modelFolder) {
        this.evaluators = new ArrayList<NeuralNetworkEvaluator>();
        File file = new File(modelFolder);
        if(!file.isDirectory()) {
            this.evaluators.add(new NeuralNetworkEvaluator(loadPMML(file)));
        } else {
            File[] files = file.listFiles();
            for(File f: files) {
                this.evaluators.add(new NeuralNetworkEvaluator(loadPMML(f)));
            }
        }

        activeFields = this.evaluators.get(0).getActiveFields();
        activeNames = new HashSet<String>();
        for(FieldName fn: activeFields) {
            activeNames.add(fn.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    public List<Double> compute(Map<String, String> rawDataMap) {
        Map<FieldName, FieldValue> dataMap = new HashMap<FieldName, FieldValue>();
        for(FieldName fn: activeFields) {
            String valueStr = rawDataMap.get(fn.getValue());
            if(("").equals(valueStr) || ("NA").equals(valueStr) || ("N/A").equals(valueStr)) {
                valueStr = null;
            } else {
                if(this.evaluators.get(0).getDataField(fn).getDataType() == DataType.DOUBLE) {
                    try {
                        Double.parseDouble(valueStr);
                    } catch (Exception e) {
                        valueStr = null;
                    }
                }
            }
            FieldValue value = EvaluatorUtil.prepare(this.evaluators.get(0), fn, (Object) valueStr);
            dataMap.put(fn, value);
        }
        List<Double> results = new ArrayList<Double>();
        for(NeuralNetworkEvaluator evaluator: this.evaluators) {
            Map<FieldName, Double> regressionTerm = (Map<FieldName, Double>) evaluator.evaluate(dataMap);
            results.add(regressionTerm.get(new FieldName("FinalResult")));
        }
        return results;
    }

    /**
     * @return the evaluators
     */
    public List<NeuralNetworkEvaluator> getEvaluators() {
        return evaluators;
    }

}
