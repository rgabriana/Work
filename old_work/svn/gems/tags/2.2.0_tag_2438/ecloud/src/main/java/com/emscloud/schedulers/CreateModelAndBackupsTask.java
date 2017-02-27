package com.emscloud.schedulers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.annotation.Resource;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.communicator.model.vo.AreaVO;
import com.communicator.model.vo.BuildingVO;
import com.communicator.model.vo.CampusVO;
import com.communicator.model.vo.ClientToServerVO;
import com.communicator.model.vo.CompanyVO;
import com.communicator.model.vo.FixtureVO;
import com.communicator.model.vo.FloorVO;
import com.communicator.model.vo.GatewayVO;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.EmInstanceManager;

public class CreateModelAndBackupsTask {

	private static final Logger logger = Logger
			.getLogger(CreateModelAndBackupsTask.class);
	@Resource
	CustomerManager customerManager;

	@Resource
	EmInstanceManager emInstanceManager;
	@Resource
	ModelsToDataBase modelsToDataBase;

	String dirPath = "G://uploaded/";
	String backUpDirPath = "G://backup/";

	private boolean checkForFile() {
		File dir = new File(dirPath);
		if (dir.isDirectory()) {
			String[] files = dir.list();
			if (files.length > 0) {
				return true;
			} else
				return false;
		} else {
			logger.fatal("Given file Path is not of directory. There must be problem with sync up of EMs");
			return false;
		}

	}

	private List<File> getListOfFile() {
		ArrayList<File> fileList = new ArrayList<File>();
		File dir = new File(dirPath);
		if (dir.isDirectory()) {
			String[] files = dir.list();
			for (String path : files) {
				fileList.add(new File(dirPath + path));

			}
		} else {
			logger.fatal("Given file Path is not of directory. There must be problem with sync up of EMs");
		}
		return fileList;

	}

	private void ReadAndCreateModel(File jFile) {
		File unzippedFile = unzip(jFile);
		try {
			ClientToServerVO commData = new ClientToServerVO();
			ObjectMapper mapper = new ObjectMapper();
			commData = mapper.readValue(unzippedFile, ClientToServerVO.class);
			unzippedFile.delete() ;
			// save data to database using jdbc dao

			ModelsToDataBase saveData = new ModelsToDataBase();
			modelsToDataBase.setEmInstanceID(commData.getMacAddress());
			modelsToDataBase.setVersion(commData.getMacAddress());
			modelsToDataBase.saveAreaVO((ArrayList<AreaVO>) commData.getArea());
			modelsToDataBase.saveBuildingVO((ArrayList<BuildingVO>) commData
					.getBuilding());
			modelsToDataBase.saveCampusVO((ArrayList<CampusVO>) commData.getCampus());
			modelsToDataBase.saveCompanyVO((ArrayList<CompanyVO>) commData.getCompany());
			modelsToDataBase.saveFixtureVO((ArrayList<FixtureVO>) commData.getFixture());
			modelsToDataBase.saveFloorVO((ArrayList<FloorVO>) commData.getFloor());
			modelsToDataBase.saveGatewayVO((ArrayList<GatewayVO>) commData.getGateway());

			logger.warn("Data from EM with Mac id " + commData.getMacAddress()
					+ "  has been stored sucessfully at time"
					+ Calendar.getInstance().getTime().getTime());
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			logger.fatal("Exception while parsing the json from EM communicator data");
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			logger.fatal("Exception while parsing the json from EM communicator data");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.fatal("Exception while parsing the json from EM communicator data");
			e.printStackTrace();
		} catch (Exception e) {
			logger.fatal("Unknown Exception while parsing the json from EM communicator data");
		}

	}

	private boolean takeBackUp(File jfile) {
		try {
			if (jfile.renameTo(new File(backUpDirPath, jfile.getName()))) {
				return true;
			} else {
				jfile.delete();
				logger.warn("Problem taking backup of file. Deleting the file to save duplicate processing");

			}

		} catch (Exception e) {
			jfile.delete();
			logger.warn("Problem taking backup of file. Deleting the file to save duplicate processing");
			e.printStackTrace();
		}
		return true;
	}

	public File unzip(File f) {
		File temp = new File("temp.tar");
		File jasonFile = new File("json.file");


		GZIPInputStream in = null;
		FileOutputStream out = null;
		BufferedOutputStream outputStream =null ;

		try {
			// create tar from tar.gz
			temp.createNewFile();
			jasonFile.createNewFile();
			out = new FileOutputStream(temp);
			in = new GZIPInputStream(new FileInputStream(f));

			int count;
			byte data[] = new byte[1024];

			while ((count = in.read(data)) > 0) {
				out.write(data, 0, count);
			}
			out.flush();
			// get content from tar file

			ArchiveInputStream input = new ArchiveStreamFactory()
					.createArchiveInputStream(new BufferedInputStream(
							new FileInputStream(temp)));
			if (input instanceof TarArchiveInputStream) {
				TarArchiveInputStream tarInput = (TarArchiveInputStream) input;
				TarArchiveEntry entry = tarInput.getNextTarEntry();
				while (entry != null) {
					if (entry.getName().equalsIgnoreCase("json.file")) {
						 outputStream = new BufferedOutputStream(
								new FileOutputStream(jasonFile));
						byte[] content = new byte[(int) entry.getSize()];

						tarInput.read(content);

						if (content.length > 0) {
							IOUtils.copy(new ByteArrayInputStream(content),
									outputStream);
						}
					}
					entry = tarInput.getNextTarEntry();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArchiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				outputStream.close() ;
				out.close();
				in.close();
				temp.delete() ;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return jasonFile;
	}

	public void runTask() {
		ArrayList<File> fileLists;

		if (checkForFile()) {
			fileLists = (ArrayList<File>) getListOfFile();
			if (fileLists != null || !fileLists.isEmpty()) {
				for (File f : fileLists) {
					try {
						ReadAndCreateModel(f);
						takeBackUp(f);
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}
			}
		} else {
			logger.info("There are no files will check after some time again.");
		}
	}

}
