package my.winapp.extension;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.helpers.collection.Iterators;
import org.neo4j.shell.util.json.JSONObject;

@Path("/servicetwo")
public class Neo4jExtensionTwo {
	
	private final ObjectMapper objectMapper = new ObjectMapper();

    enum Labels implements Label {
        User
    }

    enum RelTypes implements RelationshipType {
        POSTED
    }

	
	@GET
    @Path("/helloworld")
    public String helloWorld() {
        return "Hello World from servicetwo!";
    }
	
	//https://github.com/dmontag/neo4j-unmanaged-extension-template/blob/master/src/main/java/com/neo4j/example/extension/MyService.java#L32
		
	@GET
    @Path("/userone/{uuid}")
    public Response getUserByUUID(@PathParam("uuid") UUID uuid, @Context GraphDatabaseService db) throws IOException {
		//Result result = db.execute("MATCH (p:User) WHERE p.uuid = {n} RETURN p.userName",
		Result result = db.execute("MATCH (p:User) WHERE p.uuid = '"+ uuid +"' RETURN p.userName",
                Collections.<String, Object>singletonMap("p", uuid));
        List<String> userInfos = new ArrayList<>();
        for (Map<String, Object> item : Iterators.asIterable(result)) {
        	userInfos.add((String) item.get("p.userName"));
        }
        return Response.ok().entity(objectMapper.writeValueAsString(userInfos)).build();
        
        //RESULT : ["bakrib"]
    }
	
	//UPDATE PROPERTY : HARDCODE
	
	@GET
    @Path("/user_update/{name}")
    public Response updateProperty(@PathParam("name") String name, @Context GraphDatabaseService db) throws IOException {
		Result result = db.execute("MATCH (p:User) WHERE p.userName = {n} SET p.displayName = 'Taylor' RETURN p.displayName",
                Collections.<String, Object>singletonMap("n", name));
        List<String> friendNames = new ArrayList<>();
        for (Map<String, Object> item : Iterators.asIterable(result)) {
            friendNames.add((String) item.get("p.displayName"));
        }
        return Response.ok().entity(objectMapper.writeValueAsString(friendNames)).build();
        
        //RESULT : ["Taylor"]
    }
	
	//UPDATE LABEL
	
	@GET
    @Path("/user_update_label/{name}")
	@Produces("application/json")
    public Response updateLabel(@PathParam("name") String name, @Context GraphDatabaseService db) throws IOException {
		Result result = db.execute("MATCH (n:User) WHERE n.userName = {n} REMOVE n:User SET n:" + name + " RETURN n.displayName",
				 Collections.<String, Object>singletonMap("n", name));
        List<String> displayNames = new ArrayList<>();
        for (Map<String, Object> item : Iterators.asIterable(result)) {
        	displayNames.add((String) item.get("n.displayName"));
        }
        return Response.ok().entity(objectMapper.writeValueAsString(displayNames)).build();
        
        //RESULT : ["Bakri Bakar"]
    }
	
	@GET
    @Path("/postphotobyname/{name}")
	@Produces("application/json")
    public Response getPostPhotoByName(@PathParam("name") String name, @Context GraphDatabaseService db) throws IOException {
		Result result = db.execute("MATCH (p:PostPhoto) WHERE p.name= '" + name + "' SET p:hashtagLabel RETURN p.caption",
				 Collections.<String, Object>singletonMap("n", name));
        List<String> displayNames = new ArrayList<>();
        for (Map<String, Object> item : Iterators.asIterable(result)) {
        	displayNames.add((String) item.get("p.caption"));
        }
        return Response.ok().entity(objectMapper.writeValueAsString(displayNames)).build();
        
        //RESULT : ["Happy Leap Year everyone! #29Feb 😂"]
    }
	
	@GET
    @Path("/postphoto/{postId}")
	@Produces("application/json")
    public Response getPostPhoto(@PathParam("postId") UUID postId, @Context GraphDatabaseService db) throws IOException {
		Result result = db.execute("MATCH (n:PostPhoto) WHERE n.postId = '"+ postId +"' RETURN n.caption",
				 Collections.<String, Object>singletonMap("n", postId));
        List<String> displayNames = new ArrayList<>();
        for (Map<String, Object> item : Iterators.asIterable(result)) {
        	displayNames.add((String) item.get("n.caption"));
        }
        return Response.ok().entity(objectMapper.writeValueAsString(displayNames)).build();
        
        //RESULT : ["Happy Leap Year everyone! #29Feb 😂"]
    }
	
	//TODO : need to receive two parameters - postId & hashtag 
	//TODO : need to return node, not 1 property only.
	
	@GET
    @Path("/post_add_label/{postId}")
	@Produces("application/json")
    public Response addPostLabel(@PathParam("postId") UUID postId, @Context GraphDatabaseService db) throws IOException {
		Result result = db.execute("MATCH (n:PostPhoto) WHERE n.postId = '"+ postId +"' SET n:hashtagLabel RETURN n.caption",
				 Collections.<String, Object>singletonMap("n", postId));
        List<String> displayNames = new ArrayList<>();
        for (Map<String, Object> item : Iterators.asIterable(result)) {
        	displayNames.add((String) item.get("n.caption"));
        }
        return Response.ok().entity(objectMapper.writeValueAsString(displayNames)).build();
        
        //RESULT : ["Happy Leap Year everyone! #29Feb 😂"]
    }
	
	
	
	/**TODO
	 * add new label - addlabel
	 *  
	 */
	
	@GET
    @Path("/user/{name}")
    public Response getFriendsCypher(@PathParam("name") String name, @Context GraphDatabaseService db) throws IOException {
		Result result = db.execute("MATCH (p:User) WHERE p.userName = {n} RETURN p.displayName",
                Collections.<String, Object>singletonMap("n", name));
        List<String> friendNames = new ArrayList<>();
        for (Map<String, Object> item : Iterators.asIterable(result)) {
            friendNames.add((String) item.get("p.displayName"));
        }
        return Response.ok().entity(objectMapper.writeValueAsString(friendNames)).build();
        
      //RESULT : ["Bakri Bakar"]
    }
	
	@GET
    @Path("/userreturnnodes/{name}")
    public Response getAllUserNodes(@PathParam("name") String name, @Context GraphDatabaseService db) throws IOException {
		Result result = db.execute("MATCH (p:User) WHERE p.userName = {n} RETURN p",
                Collections.<String, Object>singletonMap("p", name));
//        List<String> userNodes = new ArrayList<>();
//        for (Map<String, Object> item : Iterators.asIterable(result)) {
//        	userNodes.add((String) item.get("p"));
//        }
        //return Response.ok().entity(objectMapper.writeValueAsString(userNodes)).build();
        //return Response.ok().entity(userNodes).build();
		
		return Response.ok().entity(result).build();
        
        
      //RESULT : 
    }
	
	

}
