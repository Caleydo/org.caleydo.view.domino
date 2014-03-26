/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.rnb.internal.util;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.caleydo.core.data.collection.EDataClass;
import org.caleydo.core.data.collection.EDataType;
import org.caleydo.core.data.collection.column.container.CategoricalClassDescription;
import org.caleydo.core.io.ColumnDescription;
import org.caleydo.core.io.DataDescription;
import org.caleydo.core.io.DataSetDescription;
import org.caleydo.core.io.DataSetDescription.ECreateDefaultProperties;
import org.caleydo.core.io.IDSpecification;
import org.caleydo.core.io.NumericalProperties;
import org.caleydo.core.io.ParsingRule;
import org.caleydo.core.io.ProjectDescription;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.color.ColorBrewer;


public class MovieLensProjectCreator implements Runnable {
	private static final String BASE = "D:/Downloads/ml-100k/ml-100k/";

	private ProjectDescription projectDescription = new ProjectDescription();
	private final String outputFile = BASE + "movielens.xml";

	private IDSpecification movies, users;

	public static void main(String[] args) {
		MovieLensProjectCreator generator = new MovieLensProjectCreator();
		generator.run();
	}

	protected void setUpDataSetDescriptions() {

		movies = new IDSpecification();
		movies.setIdType("MOVIES");
		movies.setIdCategory("MOVIES");

		users = new IDSpecification();
		users.setIdType("USERS");
		users.setIdCategory("USERS");

		projectDescription.add(loadRating());
		projectDescription.add(loadUsers());
		projectDescription.add(loadMovies());
	}

	private DataSetDescription loadRating() {
		DataSetDescription rating = new DataSetDescription(ECreateDefaultProperties.NUMERICAL);
		rating.setDataSetName("Rating");
		rating.setColor(Color.LIGHT_GRAY);
		rating.setDataSourcePath(BASE + "rating.tsv");
		rating.setNumberOfHeaderLines(1);
		rating.setRowIDSpecification(users);
		rating.setColumnIDSpecification(movies);
		final NumericalProperties n = new NumericalProperties();
		n.setMin(1.f);
		n.setMax(5.f);
		rating.setDataDescription(new DataDescription(EDataClass.NATURAL_NUMBER, EDataType.INTEGER, n));

		ParsingRule parsingRule = new ParsingRule();
		parsingRule.setFromColumn(1);
		parsingRule.setParseUntilEnd(true);
		parsingRule.setColumnDescripton(new ColumnDescription());
		rating.addParsingRule(parsingRule);

		return rating;
	}

	private DataSetDescription loadUsers() {
		DataSetDescription rating = new DataSetDescription();
		rating.setDataSetName("Users");
		rating.setColor(Color.LIGHT_BLUE);
		rating.setDataSourcePath(BASE + "users.tsv");
		rating.setNumberOfHeaderLines(1);
		rating.setRowIDSpecification(users);
		rating.setColumnIDSpecification(new IDSpecification("USER_COL", "USER_COL"));

		// age gender occupation zip code mean_rating median_rating num_rating
		rating.addParsingRule(rule("age", 1, EDataClass.NATURAL_NUMBER, EDataType.INTEGER));
		rating.addParsingRule(crule("gender", 2, "M", "Male", "F", "Female"));
		rating.addParsingRule(crule("occupation", 3, "administrator", "Administrator", "artist", "Artist", "doctor",
				"Doctor", "educator", "Educator", "engineer", "Engineer", "entertainment", "Entertainment",
				"executive", "Executive", "healthcare", "Healthcare", "homemaker", "Homemaker", "lawyer", "Lawyer",
				"librarian", "Librarian", "marketing", "Marketing", "none", "None", "other", "Other", "programmer",
				"Programmer", "retired", "Retired", "salesman", "Salesman", "scientist", "Scientist", "student",
				"Student", "technician", "Technician", "writer", "Writer"));
		rating.addParsingRule(rule("zipcode", 4, EDataClass.NATURAL_NUMBER, EDataType.INTEGER));
		rating.addParsingRule(rule("mean_rating", 5, EDataClass.REAL_NUMBER, EDataType.FLOAT));
		rating.addParsingRule(rule("median_rating", 6, EDataClass.REAL_NUMBER, EDataType.FLOAT));
		rating.addParsingRule(rule("num_rating", 7, EDataClass.NATURAL_NUMBER, EDataType.INTEGER));
		return rating;
	}

	private DataSetDescription loadMovies() {
		DataSetDescription rating = new DataSetDescription();
		rating.setDataSetName("Movies");
		rating.setColor(Color.LIGHT_RED);
		rating.setDataSourcePath(BASE + "items.tsv");
		rating.setNumberOfHeaderLines(1);
		rating.setRowIDSpecification(movies);
		rating.setColumnIDSpecification(new IDSpecification("MOVIE_COL", "MOVIE_COL"));

		// movie title release date video release date IMDb URL unknown Action Adventure Animation Children's Comedy
		// Crime Documentary Drama Fantasy Film-Noir Horror Musical Mystery Romance Sci-Fi Thriller War Western
		// release_year mean_rating median_rating num_rating
		rating.addParsingRule(rule("movie_title", 1, EDataClass.UNIQUE_OBJECT, EDataType.STRING));
		// rating.addParsingRule(rule("release_date", 2, EDataClass.UNIQUE_OBJECT, EDataType.STRING));
		// rating.addParsingRule(rule("video release date", 3, EDataClass.NATURAL_NUMBER, EDataType.INTEGER));
		rating.addParsingRule(rule("IMDb URL", 4, EDataClass.UNIQUE_OBJECT, EDataType.STRING));
		int i = 5;
		for (String genre : Arrays.asList("unknown", "Action", "Adventure", "Animation", "Children's", "Comedy",
				"Crime", "Documentary", "Drama", "Fantasy", "Film-Noir", "Horror", "Musical", "Mystery", "Romance",
				"Sci-Fi", "Thriller", "War", "Western")) {
			rating.addParsingRule(crule(genre, i++, "0", "No", "1", "Yes"));
		}
		rating.addParsingRule(rule("release_year", i++, EDataClass.NATURAL_NUMBER, EDataType.INTEGER));
		rating.addParsingRule(rule("mean_rating", i++, EDataClass.REAL_NUMBER, EDataType.FLOAT));
		rating.addParsingRule(rule("median_rating", i++, EDataClass.REAL_NUMBER, EDataType.FLOAT));
		rating.addParsingRule(rule("num_rating", i++, EDataClass.NATURAL_NUMBER, EDataType.INTEGER));
		return rating;
	}

	private ParsingRule crule(String label, int col, String... categories) {
		ParsingRule rule = rule(label, col, EDataClass.CATEGORICAL,EDataType.STRING);
		DataDescription data = rule.getColumnDescripton().getDataDescription();
		CategoricalClassDescription<String> c = new CategoricalClassDescription<String>(EDataType.STRING);
		data.setCategoricalClassDescription(c);

		final int count = categories.length / 2;
		List<Color> colors = ColorBrewer.Set3.getColors(count);
		for (int i = 0; i < categories.length; i += 2) {
			String cat = categories[i];
			String name = categories[i + 1];
			c.addCategoryProperty(cat, name, colors.get(i / 2));
		}
		return rule;
	}

	private static ParsingRule rule(String label, int col, EDataClass clazz, EDataType type) {
		ParsingRule rule = new ParsingRule();
		rule.setFromColumn(col);
		rule.setToColumn(col);
		rule.setColumnDescripton(new ColumnDescription(new DataDescription(clazz, type)));
		return rule;
	}

	/**
	 * Triggers the loading of the <code>DataSetDescriptions</code> and the serialization.
	 */
	@Override
	public void run() {
		setUpDataSetDescriptions();
		serialize();
	}

	/**
	 * Serializes the elements in {@link #projectDescription} to the {@link #outputXMLFilePath}.
	 */
	private void serialize() {
		JAXBContext context = null;
		try {
			Class<?>[] serializableClasses = new Class<?>[2];
			serializableClasses[0] = DataSetDescription.class;
			serializableClasses[1] = ProjectDescription.class;

			context = JAXBContext.newInstance(serializableClasses);

			Marshaller marshaller;
			marshaller = context.createMarshaller();
			marshaller.marshal(projectDescription, new File(outputFile));
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			System.out.println("Created configuration for "
					+ projectDescription.getDataSetDescriptionCollection().size() + " datasets: " + projectDescription);
			System.out.println("Written to: " + outputFile);
		} catch (JAXBException ex) {
			throw new RuntimeException("Could not create JAXBContexts", ex);
		}
	}

}
