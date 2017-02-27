package com.motion.vo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;



@XmlRootElement(name="draw")
@XmlAccessorType(XmlAccessType.FIELD)
public class DrawVO {
	@XmlElement(name = "blobs")
	List<BlobVO> blobVO = new ArrayList();
	@XmlElement(name = "lines")
	List<LineVO> lineVO = new ArrayList();
	public List<BlobVO> getBlobVO() {
		return blobVO;
	}
	public void setBlobVO(List<BlobVO> blobVO) {
		this.blobVO = blobVO;
	}
	public List<LineVO> getLineVO() {
		return lineVO;
	}
	public void setLineVO(List<LineVO> lineVO) {
		this.lineVO = lineVO;
	}

}
