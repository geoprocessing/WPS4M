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

import java.io.File;

import org.n52.javaps.algorithm.annotation.Algorithm;
import org.n52.javaps.algorithm.annotation.ComplexInput;
import org.n52.javaps.algorithm.annotation.Execute;
import org.n52.javaps.algorithm.annotation.LiteralOutput;
import org.n52.javaps.io.literal.xsd.LiteralStringType;

import geoprocessing.io.data.binding.complex.GeneralFileBinding;

@Algorithm(version = "1.1.0",identifier = "DownloadProcess")
public class DownloadProcess {
	private File localFile;
	private String path;
	
	@ComplexInput(identifier = "Url",binding = GeneralFileBinding.class)
	public void setFile(File url) {
		this.localFile= url;
	}
	
	@Execute
	public void execute() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.path = this.localFile.getAbsolutePath();
		
	
		try {
			Thread.sleep(100000);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@LiteralOutput(identifier = "result",binding = LiteralStringType.class)
	public String getResult() {
		return this.path;
	}
	
	
}
