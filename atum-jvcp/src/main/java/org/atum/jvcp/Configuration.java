/**
 * 
 */
package org.atum.jvcp;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 2 Jan 2017
 */
public class Configuration {

	public static void main(String[] args){

		new Configuration();
	}
	
	public Configuration(){
		Gson gson = new GsonBuilder().create();
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("readers.json");
		LinkedTreeMap result = gson.fromJson(new InputStreamReader(is) , LinkedTreeMap.class);
		for(Object o : result.entrySet()){
			Entry e = (Entry) o;
			System.out.println(e.getKey()+" "+e.getValue());
			if(e.getValue() instanceof Map){
				Map m = (Map) e.getValue();
				for(Object o2 : m.entrySet()){
					Entry e2 = (Entry) o2;
					System.out.println(e2.getKey()+" "+e2.getValue());
				}
			}
		}
	}
}
