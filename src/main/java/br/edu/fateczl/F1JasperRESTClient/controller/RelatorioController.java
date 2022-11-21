package br.edu.fateczl.F1JasperRESTClient.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

@Controller
public class RelatorioController {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(name = "relatorio", value = "/relatorio", method = RequestMethod.POST)
	public ResponseEntity geraRelatorio(@RequestParam Map<String, String> params) {
		String erro = "";
		String pais = params.get("pais");
		
		Map<String, Object> paramsEntradaRelatorio = new HashMap<>();
		
		byte[] bytes = null;
		HttpHeaders header = new HttpHeaders();
		HttpStatus status = null;
		InputStreamResource resource = null;
		
		try {
			InputStream jsonInputStream = urlConnection(pais);
			JsonDataSource ds = new JsonDataSource(jsonInputStream, "temporada");
			File arquivo = ResourceUtils.getFile
					("classpath:reports/F12014LabBD20222JSON.jasper");
			JasperReport report = 
					(JasperReport) JRLoader
					.loadObjectFromFile(arquivo.getAbsolutePath());
			bytes = JasperRunManager
					.runReportToPdf(report, paramsEntradaRelatorio, ds);
		} catch (IOException | JRException e) {
			e.printStackTrace();
			erro = e.getMessage();
			status = HttpStatus.BAD_GATEWAY;
		} finally {
			if (erro != null) {
				status = HttpStatus.OK;
				ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
				resource = new InputStreamResource(bais);
				header.setContentType(MediaType.APPLICATION_PDF);
				header.setContentLength(bytes.length);
			}

		}
		return new ResponseEntity(resource, header, status);
	}
	
	private InputStream urlConnection(String pais) throws MalformedURLException, IOException {
		String sURL = "http://localhost:8080/F1WS/api/temporada/"+pais; 
		
		URL url = new URL(sURL);
		URLConnection connection = url.openConnection();
		connection.setConnectTimeout(60 * 1000);
		connection.setReadTimeout(60 * 1000);
		InputStream jsonInputStream = connection.getInputStream();
		
		return jsonInputStream;
	}
}
