package com.BiShare.Servlet;

import java.io.IOException;
import java.security.DomainLoadStoreParameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.print.Doc;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

/**
 * Servlet implementation class BiShareServlet
 */
@WebServlet("/BiShareServlet")
public class BiShareServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// private memebers which are related to the mongodb
	private static MongoClient mongoClient;
	private static MongoDatabase db;

	public static void mongoDBInitiation() {
		try {
			// MongoDB is listening on port 27017 at this stage
			mongoClient = new MongoClient("localhost", 27017);
			db = mongoClient.getDatabase("biShare");
			// Get database. If the database doesn’t exist, MongoDB will create
			// it.

			MongoCollection customers_coll = db.getCollection("customers");
			customers_coll.drop();
			MongoCollection dockingbays_coll = db.getCollection("dockingbays");
			dockingbays_coll.drop();

			// initiating collection docking bays:
			Document dbDocument;
			String json;
			json = " {" + "'d_id':1001, 'latitude':-37.788683, 'longitude':175.316946, " + "'bicycles':["
					+ "200001, 200002, 200003" + "]}";
			dbDocument = Document.parse(json);
			dockingbays_coll.insertOne(dbDocument);
			json = " {" + "'d_id':1002, 'latitude':-37.787871, 'longitude':175.316264, " + "'bicycles':["
					+ "200004, 200005, 200006, 200007 " + "]}";
			dbDocument = Document.parse(json);
			dockingbays_coll.insertOne(dbDocument);
			json = " {" + "'d_id':1003, 'latitude':-37.787765, 'longitude':175.318662, " + "'bicycles':["
					+ "200008, 200009 " + "]}";
			dbDocument = Document.parse(json);
			dockingbays_coll.insertOne(dbDocument);

			// initiating collection customers:
			json = "{" + "'c_id':'DR724688', " + "'firstname':'Steve', " + "'lastname':'Rogers', "
					+ "'occupiedbicycle':0 " + "}";
			dbDocument = Document.parse(json);
			customers_coll.insertOne(dbDocument);
			json = "{" + "'c_id':'DR724686', " + "'firstname':'Jack', " + "'lastname':'Sparrow', "
					+ "'occupiedbicycle':200010 " + "}";
			dbDocument = Document.parse(json);
			customers_coll.insertOne(dbDocument);

			// display all current data collection:
			FindIterable<Document> cursorDocJSON = dockingbays_coll.find();
			for (Document doc : cursorDocJSON) {
				System.out.println(doc);
			}
			/*
			 * // cursorDocJSON = customers_coll.find(); for(Document
			 * doc:cursorDocJSON){ System.out.println(doc); }//
			 */

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void init() throws ServletException {
		// initialize the mongoDB
		mongoDBInitiation();
	}

	/**
	 * @see HttpServlet#HttpServlet()
	 *
	 */
	// public BiShareServlet() {
	// super();
	// // TODO Auto-generated constructor stub
	// }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		/*/ Auto-generated method start
		response.getWriter().append("Served at: ").append(request.getContextPath());
		// Auto-generated method end //*/
		
		String type = request.getParameter("type");
		
		if(type.equals("map")){
			doMap(request,response);
		}else if("bicycle".equals(type)){
			doBicycle(request,response);
		}
		
		
	
	}

	private void doMap(HttpServletRequest request, HttpServletResponse response) {
		
		MongoCollection dockingbays_coll = db.getCollection("dockingbays");
		FindIterable<Document> docks = dockingbays_coll.find();
		List<String> result = new ArrayList<>();
		for(Document doc:docks){
			result.add(doc.toJson());
		}
		response.setContentType("application/json");
		try {
			response.getWriter().println(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	
	private void doBicycle(HttpServletRequest request, HttpServletResponse response){
		String dockingBayID = request.getParameter("dockingBayID");
		String customerID = request.getParameter("customerID");
		
		MongoCollection dockingbays_coll = db.getCollection("dockingbays");
		MongoCollection customers_coll = db.getCollection("customers");
		System.out.println("========================================before========================================");
		FindIterable<Document> docks = dockingbays_coll.find();
		for(Document doc:docks){
			System.out.println(doc);
		}
		
		FindIterable<Document> users = customers_coll.find();
		for(Document doc:users){
			System.out.println(doc);
		}
		System.out.println("======================================================================================");
		// display all current data collection:
		response.setContentType("application/json");
		FindIterable<Document> cursorCustomer = customers_coll.find(
				new Document("c_id", customerID).append("occupiedbicycle", 0));
		Iterator<Document> iterator = cursorCustomer.iterator();
		
		if(iterator.hasNext()){
			Document customer=iterator.next();
			FindIterable<Document> cursorDockingBay = dockingbays_coll.find(Filters.eq("d_id", Integer.parseInt(dockingBayID)));
			cursorDockingBay.forEach(new Block<Document>() {
				@Override
				public void apply(Document dockingBay) {
					
					try {
						ArrayList<Integer> bicycles = dockingBay.get("bicycles",ArrayList.class);
						if (bicycles!=null&&bicycles.size()>0) {
							Integer bicycleId = bicycles.remove(0);
							System.out.println("bicycleId="+bicycleId);
							customers_coll.updateOne(new Document("c_id", customerID).append("occupiedbicycle", 0),
							        new Document("$set", new Document("occupiedbicycle", bicycleId)));
							
							//update dock bay bicycles
							dockingbays_coll.updateOne(new Document("d_id", Integer.parseInt(dockingBayID)),
															new Document("$set", new Document("bicycles", bicycles)));
						
							response.getWriter().println("update successfully");
							
						}else {
							response.getWriter().println("dockingBay has no bicycle left");
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
			});
			
		
		}else{
			try {
				response.getWriter().println("not found customer with occupiedbicycle=0");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("========================================after========================================");
		FindIterable<Document> docksafter = dockingbays_coll.find();
		for(Document doc:docksafter){
			System.out.println(doc);
		}
		
		FindIterable<Document> usersafter = customers_coll.find();
		for(Document doc:usersafter){
			System.out.println(doc);
		}
		System.out.println("======================================================================================");
	}

}
/*
 * / start MongoDB first by ... you know cd ./mongodb mongod
 *
 * start jetty server by java -jar start.jar jetty.port=8088
 * 
 * use http://localhost:8088/BiShareJettyServlet/BiShareServlet to access page.
 * // //
 */
