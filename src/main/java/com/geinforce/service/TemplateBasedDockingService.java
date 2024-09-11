package com.geinforce.service;

import lombok.extern.slf4j.Slf4j;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.json.JSONArray;
import org.json.JSONObject;
/*import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
*/
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;



@Service
@Slf4j
public class TemplateBasedDockingService {

    @Autowired
    FileService fileService;
    @Value("${file.user}")
    String userDirectory;
    public String templateBasedDocking(String jobName,String email){
        String proteinFilePath = fileService.getFilePath(jobName,email,"protein");
        String ligandFilePath = fileService.getFilePath(jobName,email,"ligand");
        System.out.println("ligandFile path"+ligandFilePath);
        System.out.println("proteinFilePath"+proteinFilePath);


       //for protein file    START
        String sequence =  getProteinStructure(proteinFilePath);
        System.out.println("structure list"+sequence);
        String pdbId = getProteinPDBId(sequence);
        String templatePdbFile = getProteinTemplate(pdbId,jobName,email);
        System.out.println("Template file name="+templatePdbFile);
        //for protein file    END


        //for ligand file    START
        //find the template for fitock of ligand file
        String ligandConvert = fileService.convertPdbToMol2(ligandFilePath,ligandFilePath.replace(".pdb",".mol2"));
        System.out.println("result="+ligandConvert);
        try {
            String smiles = fileService.convertLigandToSMILES(ligandFilePath.replace(".pdb",".mol2"));
            System.out.println("Smiles="+smiles);

            List<String>listPDBId_chembl = searchLigandRCSBPDB_chembl(smiles);
            System.out.println("List of ligand rcsbpdb="+listPDBId_chembl);
            for(String ligId:listPDBId_chembl){
                downloadPdbFileForLigand(ligId,
                        new File(ligandFilePath).getParent()+File.separator+ligId+"_template.pdb");
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Error in geting smiles="+e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //for protein file    END
        return "Success:Template based docking done";

    }
    public String getProteinStructure(String filePath){
        String sequence="";
        try {
            PDBFileReader pdbReader = new PDBFileReader();

            Structure structure = pdbReader.getStructure(filePath);
            for (Chain chain : structure.getChains()) {
                sequence = chain.getAtomSequence();

                System.out.println("get sequence="+sequence);
                System.out.println("Chain " + chain.getId() + " Sequence: " + sequence);

            }
        }catch(Exception e){
            System.out.println("error in getting structure"+e.getMessage());
        }
        return sequence;
    }

    public String getProteinPDBId(String sequence) {
        String pdbId = "";
        try {
            String database = "pdbaa";

           // String encodedSequence = URLEncoder.encode(sequence, StandardCharsets.UTF_8);
            //String templateFilePath = userDirectory + File.separator + email + File.separator + "protein";
            File tempFile = File.createTempFile("sequence", ".fasta");
            System.out.println("temp File Path=" + tempFile.getAbsolutePath());
            try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
                writer.println(">query");
                writer.println(sequence);
            }

            String blastCommand = String.format("C:\\Program Files\\NCBI\\blast-2.14.0+\\bin\\blastp.exe -query %s -db %s -outfmt \"6 sseqid\"",
                    tempFile.getAbsolutePath(),
                    database);
            System.out.println("command="+blastCommand);
            ProcessBuilder pb = new ProcessBuilder(blastCommand.split(" "));
            pb.environment().put("BLASTDB", "D:\\RMI\\Blast DB\\pdbaa");
            pb.redirectErrorStream(true);  // merges standard error and standard output
            Process blastProcess = pb.start();
            blastProcess.waitFor();
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(blastProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("line"+line);
                    sb.append(line).append("\n");
                    if (line.startsWith("pdb|")) {
                        pdbId = line.split("\\|")[1]; // Get the PDB ID
                        break;
                    }
                }
            }

            System.out.println("Template file content=" + sb.toString());
            System.out.println("PDB ID=" + pdbId);
            Files.delete(Path.of(tempFile.getAbsolutePath()));
        } catch (Exception e) {
            System.out.println("Error in loading template=" + e.getMessage());
            return "";
        }

        return pdbId;
    }
    public String getProteinTemplate(String pdbId,String jobName,String email){

        String templateFilePath =  userDirectory + File.separator+email+File.separator+"protein"+File.separator+"proteinTemplate";
        String url = "ftp://ftp.wwpdb.org/pub/pdb/data/structures/all/pdb/pdb" + pdbId.toLowerCase() + ".ent.gz";
        //System.out.println("URL to find template="+url);
        Path dirPath = Paths.get(templateFilePath);
        Path outputPath = dirPath.resolve(jobName+"_protein_"+pdbId.toLowerCase() + ".ent.gz");

        try {
            // check if directory exists, if not, create it
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            try (InputStream in = new URL(url).openStream()) {
                Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
                //System.out.println("PDB file downloaded to: " + outputPath.toString());
                String outputFile = outputPath.toString().replace(".gz", "");
                fileService.decompressGzipFile(outputPath.toString(), outputFile);
                Files.delete(outputPath);
                //System.out.println("PDB .gz file deleted");
               return  fileService.renameFileExtensionEntToPDB(outputFile, ".pdb");
            } catch (IOException e) {
                System.out.println("error in getting protein template="+e.getMessage());
               // e.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("error in creating directory="+e.getMessage());
            //e.printStackTrace();
        }
        return "";
    }

    public List<String>searchLigandRCSBPDB(String smiles){
        try {
            String smilesString = "c1ccc(cc1)C1=[N]=C(N([C@@H]1c1ccccc1)c1ccccc1N)c1ccc(cc1)Cl";
            URL url = new URL("https://www.ebi.ac.uk/pdbe/graph-api/pdb/molecule/search/smiles/" + smilesString);
            System.out.println("url for serach ligand="+url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Request was successful!");
                String response = new String(conn.getInputStream().readAllBytes());
                System.out.println(response);
                // Parse the response to extract PDB IDs or other relevant information
            } else {
                System.out.println("Failed with HTTP error code: " + responseCode);
                System.out.println("Server response: " + new String(conn.getErrorStream().readAllBytes()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }






      /*  try{
        URL url = new URL("https://search.rcsb.org/rcsbsearch/v2/query");

        // Create the connection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Define the query for the ligand using SMILES
      //  String smiles = "YOUR_SMILES_STRING_HERE"; // Replace with your SMILES string
        JSONObject query = new JSONObject();
        query.put("query", new JSONObject()
                .put("type", "terminal")
                .put("service", "chemical")
                .put("parameters", new JSONObject()
                        .put("descriptor_type", "SMILES")
                        .put("descriptor", smiles)
                        .put("match_type", "graph-strict")));
        query.put("return_type", "entry");

        // Send the request
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = query.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Get the response
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            JSONObject responseJson = new JSONObject(new String(conn.getInputStream().readAllBytes()));
            System.out.println("GOt json="+responseJson);
            JSONArray identifiers = responseJson.getJSONArray("identifiers");
            for (int i = 0; i < identifiers.length(); i++) {
                System.out.println("PDB ID: " + identifiers.getJSONObject(i).getString("identifier"));
            }
        } else {
            System.out.println("Failed with HTTP error code: " + responseCode);
            System.out.println("Failed with HTTP error code: " + conn.getResponseMessage());
            System.out.println("Server response: " + new String(conn.getErrorStream().readAllBytes()));
        }
    } catch (Exception e) {
        e.printStackTrace();
    }*/
        return null;
    }
    public List<String> searchLigandRCSBPDB_chembl(String smiles){
        String PDB_SEARCH_URL = "https://search.rcsb.org/rcsbsearch/v2/query";
        RestTemplate restTemplate = new RestTemplate();

        // Define the payload
        String payload = "{\"query\":{\"type\":\"terminal\",\"label\":\"full_text\",\"service\":\"full_text\",\"parameters\":{\"value\":\"c1ccc(-c2nc3c(-c4nc5ccccc5o4)cccc3o2)cc1\"}},\"return_type\":\"entry\",\"request_options\":{\"paginate\":{\"start\":0,\"rows\":25},\"results_content_type\":[\"experimental\"],\"sort\":[{\"sort_by\":\"score\",\"direction\":\"desc\"}],\"scoring_strategy\":\"combined\"}}";

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        // Make the POST request
        ResponseEntity<String> response = restTemplate.exchange(PDB_SEARCH_URL, HttpMethod.POST, entity, String.class);
        System.out.println("result response="+ response.getBody());

        JSONObject jsonObject = new JSONObject(response.getBody());
        JSONArray resultSet = jsonObject.getJSONArray("result_set");
        List<String> pdbIdList = new ArrayList<>();
        IntStream.range(0, resultSet.length())
                .forEach(i -> {
                    JSONObject result = resultSet.getJSONObject(i);
                    pdbIdList.add(result.getString("identifier"));
                });
        System.out.println("list result="+pdbIdList);
        return pdbIdList;
    }
    private  JSONObject getJSONResponse(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder response = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            response.append(output);
        }
        conn.disconnect();

        return new JSONObject(response.toString());
    }

    public void downloadPdbFileForLigand(String pdbId, String outputPath) throws Exception {
        String pdbUrl = "https://files.rcsb.org/download/" + pdbId + ".pdb";
        try (InputStream in = new URL(pdbUrl).openStream()) {
            Files.copy(in, Paths.get(outputPath));
            fileService.convertPdbToMol2(outputPath,outputPath.replace(".pdb",".mol2"));
            Files.delete(Paths.get(outputPath));
        }
    }
}
