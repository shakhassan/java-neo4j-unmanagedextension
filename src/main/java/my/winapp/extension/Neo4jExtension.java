package my.winapp.extension;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.helpers.collection.Iterators;
import org.neo4j.string.UTF8;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;

@Path("/service")
public class Neo4jExtension {
	
	private final GraphDatabaseService database;
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	public Neo4jExtension( @Context GraphDatabaseService database )
    {
        this.database = database;
    }
	
	//http://neo4j.com/docs/java-reference/current/#tutorials-java-embedded
		
	@GET
    @Produces( MediaType.TEXT_PLAIN )
    @Path( "/{nodeId}" )
    public Response hello( @PathParam( "nodeId" ) long nodeId )
    {
        // Do stuff with the database
        return Response.status( Status.OK ).entity( UTF8.encode( "Hello World, nodeId=" + nodeId ) ).build();
    }

	
	@GET
    @Path("/helloworld")
    public String helloWorld() {
        return "Hello World Ahoi!";
    }
	
	//https://github.com/dmontag/neo4j-unmanaged-extension-template/blob/master/src/main/java/com/neo4j/example/extension/MyService.java
	
	@GET
    @Path("/post/{name}")
    public Response getFriendsCypher(@PathParam("name") String name, @Context GraphDatabaseService  db) throws IOException {
        Result result = db.execute("MATCH (p:PostPhoto) WHERE p.name = {n} RETURN p.caption AS caption",
                Collections.<String, Object>singletonMap("n", name));
        
        //TODO NEED TO REPAIR
        List<String> caption = new ArrayList<>();
        for (Map<String, Object> item : Iterators.asIterable(result)) {
        	caption.add((String) item.get("caption"));
        }
        return Response.ok().entity(objectMapper.writeValueAsString(caption)).build();
        
        //RESULT : ["KL #kualalumpur"]
    }
	
	//http://www.markhneedham.com/blog/2015/08/10/neo4j-2-2-3-unmanaged-extensions-creating-gzipped-streamed-responses-with-jetty/
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/postphoto")
    public Response allPost() throws IOException {
        StreamingOutput stream = streamQueryResponse("MATCH (n:PostPhoto) RETURN n.postId AS postId");
        return Response.ok().entity(stream).type(MediaType.APPLICATION_JSON).build();
        
        //RESULT : [{"postId":"50b12d35-94fd-4297-bb18-e6040d7b7109"},{"postId":"55b12d35-94fd-4297-bb18-e6040d7b7109"}]
    }
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/postphotoreturnnodes")
    public Response allPostNodes() throws IOException {
        StreamingOutput stream = streamQueryResponse("MATCH (n:PostPhoto) RETURN n");
        return Response.ok().entity(stream).type(MediaType.APPLICATION_JSON).build();
        
        //RESULT : [{"n":"Node[0]"},{"n":"Node[1]"},{"n":"Node[190]"},{"n":"Node[193]"}]
    }
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/postphoto/{name}")
    public Response postById(@PathParam("name") String name) throws IOException {
        StreamingOutput stream = streamQueryResponse("MATCH (p:PostPhoto) WHERE p.name= '" + name + "' RETURN p.caption AS caption");
		//StreamingOutput stream = streamQueryResponse("MATCH (p:PostPhoto {name: {name} }) RETURN p.caption AS postCaption");
        return Response.ok().entity(stream).type(MediaType.APPLICATION_JSON).build();
        
        //RESULT : [{"caption":"Happy Leap Year everyone! #29Feb \uD83D\uDE02"}]
    }
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/userusername")
    public Response allNodesReturnProperty() throws IOException {
        StreamingOutput stream = streamQueryResponse("MATCH (n:User) RETURN n.userName AS UserName");
		//StreamingOutput stream = streamQueryResponse("MATCH (n:User) RETURN n.postId, n.name, n.s3Key, n.dateCreated, n.caption");
        return Response.ok().entity(stream).type(MediaType.APPLICATION_JSON).build();
        
        //RESULT : [{"UserName":"joshua_keng"},{"UserName":"aru666"},{"UserName":"tinaherself"}]
    }
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/usernode")
    public Response allNodesReturnNodes() throws IOException {
        StreamingOutput stream = streamQueryResponse("MATCH (n:User) RETURN n.userName, n.displayName");
        return Response.ok().entity(stream).type(MediaType.APPLICATION_JSON).build();
        
        //RESULT : [{"n.userName":"joshua_keng","n.displayName":"Joshua Keng"},{"n.userName":"aru666","n.displayName":"Aru Tom"}]
    }
	
	private StreamingOutput streamQueryResponse(final String query) {
        return new StreamingOutput() {
                @Override
                public void write(OutputStream os) throws IOException, WebApplicationException {
                    JsonGenerator jg = objectMapper.getJsonFactory().createJsonGenerator(os, JsonEncoding.UTF8);
                    jg.writeStartArray();
 
                    writeQueryResultTo(query, jg);
 
                    jg.writeEndArray();
                    jg.flush();
                    jg.close();
                }
            };
    }
 
    private void writeQueryResultTo(String query, JsonGenerator jg) throws IOException {
        try (Result result = database.execute(query)) {
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
 
                jg.writeStartObject();
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    jg.writeFieldName(entry.getKey());
                    jg.writeString(entry.getValue().toString());
                }
                jg.writeEndObject();
            }
        }
    }

}
