package com.colorado.denver;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.colorado.denver.controller.HibernateController;
import com.colorado.denver.model.Exercise;
import com.colorado.denver.model.Home;

/*
 * Keep this class clean! only main method and temporary experiments!
 */

@EnableWebMvc
@SpringBootApplication
public class DenverApplication {

	private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DenverApplication.class);

	public static void main(String[] args) throws IOException {
		LOGGER.info("Starting app!");
		SpringApplication.run(DenverApplication.class, args);

		// //
		// Hibernate Usage //
		// //
		LOGGER.info("Starting with Hibernate experiments..");
		HibernateController hibCtrl = new HibernateController();
		/* Add few home records in database */
		Home home1 = new Home();
		hibCtrl.addEntity(home1);

		Exercise exc = new Exercise();
		exc.setTitle("Hello i should have overriden exervise 1");
		String ecxId = hibCtrl.addEntity(exc);

		Exercise returnedExcercise = (Exercise) hibCtrl.getEntity(ecxId, Exercise.class);
		LOGGER.error("The title of excercise is: " + returnedExcercise.getTitle());
		LOGGER.error("The is of excercise is: " + returnedExcercise.getId());

		// get List of Entities from table
		List<Home> homes = (List<Home>) (List<?>) hibCtrl.getEntityList(Home.class);// if(weFail){system.crashAndBurn();}
		for (Iterator iterator = homes.iterator(); iterator.hasNext();) {
			Home home = (Home) iterator.next();
			LOGGER.info("ID: " + home.getId());
			LOGGER.info("Content: " + home.getContent());
			LOGGER.info("ObjectClass: " + home.getObjectClass());
		}
		LOGGER.info("--------------END OF HIBERNATE EXPERIMENTS------------------");
	}

}