/*
 * Copyright 2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
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
package geoprocessing.algorithm.annotation;

import org.n52.javaps.algorithm.annotation.Algorithm;
import org.n52.javaps.algorithm.annotation.Execute;
import org.n52.javaps.algorithm.annotation.LiteralInput;
import org.n52.javaps.algorithm.annotation.LiteralOutput;
import org.n52.javaps.io.literal.xsd.LiteralStringType;

@Algorithm(version = "1.1.0",identifier = "StringJoint")
public class StringJointProcess2 {
	private String string1,string2,result;
	
	@LiteralInput(identifier = "string1",binding = LiteralStringType.class)
	public void setX(String string1) {
		this.string1 = string1;
	}
	
	@LiteralInput(identifier = "string2",binding = LiteralStringType.class)
	public void setY(String string1) {
		this.string2 = string1;
	}
	
	@Execute
	public void execute() {
		this.result = this.string1 + " "+this.string2;
	}
	
	@LiteralOutput(identifier = "result",binding = LiteralStringType.class)
	public String getResult() {
		return this.result;
	}
	
	
}
