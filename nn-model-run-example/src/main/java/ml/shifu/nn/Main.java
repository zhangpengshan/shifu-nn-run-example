/*
 * Copyright [2013-2016] PayPal Software Foundation
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 * 
 * @author Zhang David (pengzhang@paypal.com)
 */
public class Main {

    public static void main(String[] args) throws IOException {
        NeuralNetworkModelRunner modelRunner = new NeuralNetworkModelRunner(args[0]);

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(args[1]));
            String headerStr = br.readLine();
            String[] headers = headerStr.split(",");
            String valueStr = null;
            while((valueStr = br.readLine()) != null) {
                if(valueStr.length() == 0) {
                    continue;
                }
                String[] values = valueStr.split(",");
                assert headers.length == values.length;
                Map<String, String> data = new HashMap<String, String>();
                for(int i = 0; i < values.length; i++) {
                    data.put(headers[i], values[i]);
                }

                List<Double> results = modelRunner.compute(data);
                System.out.println(results);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(br != null) {
                br.close();
            }
        }
    }

}
