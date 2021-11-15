package geoprocessing.algorithm.selfdescription;

import org.n52.javaps.algorithm.AbstractAlgorithm;
import org.n52.javaps.description.TypedProcessDescription;
import org.n52.javaps.description.impl.TypedProcessDescriptionFactory;
import org.n52.javaps.description.impl.TypedProcessDescriptionImpl;

public abstract class SelfDescriptionAlgorithm extends AbstractAlgorithm {

	protected String identifier;
    private  TypedProcessDescriptionFactory descriptionFactory;
    private TypedProcessDescriptionImpl.Builder processBuilder;
    
	@Override
	protected TypedProcessDescription createDescription() {
		String path = "/descriptions/"+identifier+".xml";
		String role="http://geoprocessing.whu.edu.cn/iotype";
		
		
		if(this.descriptionFactory ==null)
			this.descriptionFactory = new TypedProcessDescriptionFactory();
		
		processBuilder = this.descriptionFactory.process();
		
		/*
		InputStream is = SelfDescriptionAlgorithm.class.getResourceAsStream(path);
		
		try {
			ProcessOfferingDocument doc = ProcessOfferingDocument.Factory.parse(is);
			
			ProcessOffering processOffering = doc.getProcessOffering();
			ProcessDescriptionType processDescriptionType = processOffering.getProcess();
			InputDescriptionType[] inputs = processDescriptionType.getInputArray();
			
			for(InputDescriptionType input:inputs) {
				String identifier = input.getIdentifier().getStringValue();
				System.out.println("-------------"+identifier+"-------------");
				MetadataType[] metadataTypes = input.getMetadataArray();
				for(MetadataType meta:metadataTypes) {
					if(meta.getRole()!=null && meta.getRole().equals(role)) {
						System.out.println("metadata:role="+meta.getRole()+",href="+meta.getHref());
					}
					System.out.println("titile:"+meta.getTitle());
				}
				if(input.getInputArray()!=null && input.getInputArray().length>0) {
					for(InputDescriptionType inputDesc:input.getInputArray()) {
						String subId = inputDesc.getIdentifier().getStringValue();
						System.out.println(subId+"@"+identifier);
					}
				}
			}
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
		
		*/
		
		return null;
	}

	/*
	private void buildProcess(ProcessOffering processOffering) {
		String version = processOffering.getProcessVersion();
		if(version!=null)
			this.processBuilder = this.processBuilder.withVersion(version);
		
		ProcessDescriptionType processDescriptionType = processOffering.getProcess();
		
		String identifier = processDescriptionType.getIdentifier().getStringValue();
		this.processBuilder = this.processBuilder.withIdentifier(identifier);
		
		LanguageStringType[] titles = processDescriptionType.getTitleArray();
		for(LanguageStringType title:titles) {
			this.processBuilder = this.processBuilder.withTitle(title.getLang(),title.getStringValue());
		}
		
		LanguageStringType[] abstracts = processDescriptionType.getAbstractArray();
		for(LanguageStringType abs : abstracts) {
			this.processBuilder = this.processBuilder.withAbstract(abs.getLang(),abs.getStringValue());
		}
		
		InputDescriptionType[] inputDescriptionTypes = processDescriptionType.getInputArray();
		
	}
	
	private TypedProcessInputDescription buildInput(InputDescriptionType inputDescriptionType) {
		TypedLiteralInputDescriptionImpl.Builder inputBuilder = this.descriptionFactory.literalInput();
		
		DataDescriptionType dataDescriptionType = inputDescriptionType.getDataDescription();
		
		
		String identifier = inputDescriptionType.getIdentifier().getStringValue();
		inputBuilder = inputBuilder.withIdentifier(identifier);
		
		LanguageStringType[] titles = inputDescriptionType.getTitleArray();
		for(LanguageStringType title:titles) {
			inputBuilder = inputBuilder.withTitle(title.getLang(),title.getStringValue());
		}
		
		LanguageStringType[] abstracts = inputDescriptionType.getAbstractArray();
		for(LanguageStringType abs : abstracts) {
			inputBuilder = inputBuilder.withAbstract(abs.getLang(),abs.getStringValue());
		}
		
		if(dataDescriptionType instanceof LiteralDataType) {
			
		}
		
		return null;
	}
	
	private TypedProcessInputDescription buildLiteralInput(InputDescriptionType inputDescriptionType) {
		DataDescriptionType dataDescriptionType = inputDescriptionType.getDataDescription();
		
		if(!(dataDescriptionType instanceof LiteralDataType)) {
			return null;
		}
		
		LiteralDataType literalDataType = (LiteralDataType)dataDescriptionType;
		
		TypedLiteralInputDescriptionImpl.Builder inputBuilder = this.descriptionFactory.literalInput();
		
		String identifier = inputDescriptionType.getIdentifier().getStringValue();
		inputBuilder = inputBuilder.withIdentifier(identifier);
		
		LanguageStringType[] titles = inputDescriptionType.getTitleArray();
		for(LanguageStringType title:titles) {
			inputBuilder = inputBuilder.withTitle(title.getLang(),title.getStringValue());
		}
		
		LanguageStringType[] abstracts = inputDescriptionType.getAbstractArray();
		for(LanguageStringType abs : abstracts) {
			inputBuilder = inputBuilder.withAbstract(abs.getLang(),abs.getStringValue());
		}
		
		literalDataType.getFormatArray();
		
		inputBuilder.withType(liter)
		
		return null;
	}
	*/
	
}
