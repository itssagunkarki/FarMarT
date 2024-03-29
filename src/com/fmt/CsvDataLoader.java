package com.fmt;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class CsvDataLoader {
	public static final String FILE_PATH_PERSONS = "data/Persons.csv";
	public static final String FILE_PATH_ITEMS = "data/Items.csv";
	public static final String FILE_PATH_STORES = "data/Stores.csv";
	public static final String FILE_PATH_INVOICE = "data/Invoices.csv";
	public static final String FILE_PATH_INVOICE_ITEMS = "data/InvoiceItems.csv";

	/**
	 * It loads Person.csv converts it into its class and returns a hashmap of
	 * person classes.
	 * 
	 * @return HashMap
	 */

	public static HashMap<String, Person> loadPerson() {
		HashMap<String, Person> result = new HashMap<String, Person>();
		String line = null;
		try (Scanner s = new Scanner(new File(FILE_PATH_PERSONS))) {
			int numRecords = Integer.parseInt(s.nextLine());
			for (int i = 0; i < numRecords; i++) {
				line = s.nextLine();
				if (!line.trim().isEmpty()) {
					String token[] = line.split(",", -1);

					String personCode = token[0];
					String lastName = token[1];
					String firstName = token[2];

					String street = token[3];
					String city = token[4];
					String state = token[5];
					String zip = token[6];
					String country = token[7];

					int n = 8;
					List<String> emails = new ArrayList<String>();
					while (n < token.length) {
						emails.add(token[n]);
						n++;
					}
					Address address = new Address(street, city, state, zip, country);
					Person person = new Person(personCode, lastName, firstName, address, emails);

					result.put(personCode, person);

				}

			}

		} catch (Exception e) {
			throw new RuntimeException("Encountered Error on line " + line, e);
		}
		return result;

	}

	/**
	 * It loads Stores.csv converts it into its class and returns a hashmap of Store
	 * classes.
	 * 
	 * @return HashMap
	 */

	public static HashMap<String, Store> loadStores() {
		HashMap<String, Store> result = new HashMap<String, Store>();
		String line = null;

		try (Scanner s = new Scanner(new File(FILE_PATH_STORES))) {
			int numRecords = Integer.parseInt(s.nextLine());
			for (int i = 0; i < numRecords; i++) {
				line = s.nextLine();
				if (!line.trim().isEmpty()) {
					String token[] = line.split(",", -1);
					String storeCode = token[0];
					Person managerCode = SearchIdCode.searchPerson(token[1]);

					String street = token[2];
					String city = token[3];
					String state = token[4];
					String zip = token[5];
					String country = token[6];

					Address address = new Address(street, city, state, zip, country);
					Store store = new Store(storeCode, managerCode, address);

					result.put(storeCode, store);
				}

			}

		} catch (Exception e) {
			throw new RuntimeException("Encountered Error on line " + line, e);
		}
		return result;

	}

	/**
	 * It loads Items.csv converts it into its class and returns a hashmap of Item
	 * classes.
	 * 
	 * @return HashMap
	 */

	public static HashMap<String, Item> loadItems() {
		HashMap<String, Item> result = new HashMap<String, Item>();
		String line = null;
		try (Scanner s = new Scanner(new File(FILE_PATH_ITEMS))) {
			int numRecords = Integer.parseInt(s.nextLine());
			Item item = null;
			for (int i = 0; i < numRecords; i++) {
				line = s.nextLine();
				if (!line.trim().isEmpty()) {
					String token[] = line.split(",", -1);
					String itemCode = token[0];
					String itemType = token[1];
					String itemName = token[2];
					if (itemType.equals("E")) {
						String model = token[3];
						item = new Equipment(itemCode, itemType, itemName, model);
					} else if (itemType.equals("P")) {
						String unit = token[3];
						Double unitPrice = Double.parseDouble(token[4]);
						item = new Product(itemCode, itemType, itemName, unit, unitPrice);
					} else if (itemType.equals("S")) {
						Double hourlyRate = Double.parseDouble(token[3]);
						item = new Service(itemCode, itemType, itemName, hourlyRate);
					}
					result.put(itemCode, item);
				}

			}

		} catch (Exception e) {
			throw new RuntimeException("Encountered Error on line " + line, e);
		}
		return result;
	}

	/**
	 * It loads invoice data and and returns a hashmap of invoices
	 * 
	 * @return
	 */

	private static HashMap<String, Invoice> loadWithoutItems() {
		HashMap<String, Invoice> result = new HashMap<String, Invoice>();

		String line = null;
		try (Scanner s = new Scanner(new File(FILE_PATH_INVOICE))) {

			int numRecords = Integer.parseInt(s.nextLine());
			Invoice invoice = null;
			for (int i = 0; i < numRecords; i++) {
				line = s.nextLine();
				if (!line.trim().isEmpty()) {
					String token[] = line.split(",", -1);
					String invoiceCode = token[0];

					Store store = SearchIdCode.searchStore(token[1]);
					Person customer = SearchIdCode.searchPerson(token[2]);
					Person salesPerson = SearchIdCode.searchPerson(token[3]);
					LocalDate date = LocalDate.parse(token[4]);

					invoice = new Invoice(invoiceCode, store, customer, salesPerson, date, new ArrayList<Item>());
					result.put(invoiceCode, invoice);
				}

			}

		} catch (Exception e) {
			throw new RuntimeException("Encountered Error on line " + line, e);
		}
		return result;
	}

	/**
	 * It loads Items of all the invoices and arranges them based on which invoice
	 * it is from
	 * 
	 * @param HashMap
	 */

	private static HashMap<String, List<Item>> loadInvoiceItems() {
		HashMap<String, List<Item>> result = new HashMap<String, List<Item>>();
		String line = null;

		try (Scanner s = new Scanner(new File(FILE_PATH_INVOICE_ITEMS))) {
			int numRecords = Integer.parseInt(s.nextLine());
			for (int i = 0; i < numRecords; i++) {
				line = s.nextLine();
				if (!line.trim().isEmpty()) {
					String token[] = line.split(",", -1);
					String invoiceCode = token[0];
					String itemCode = token[1];

//					create an empty array list for hashmap if not exists
					if (result.get(invoiceCode) == null) {
						result.put(invoiceCode, new ArrayList<Item>());
					}

					String PurchaseOrLease = token[2];
					if (SearchIdCode.searchItem(itemCode).getItemType().equals("E")) {

						Equipment equipment = null;

						if (PurchaseOrLease.equals("P")) {
							equipment = new PurchaseEquipment((Equipment) SearchIdCode.searchItem(itemCode),
									Double.parseDouble(token[3]));
							result.get(invoiceCode).add(equipment);

						} else if (PurchaseOrLease.equals("L")) {
							Double leasePrice = Double.parseDouble(token[3]);
							LocalDate startDate = LocalDate.parse(token[4]);
							LocalDate endDate = LocalDate.parse(token[5]);
							equipment = new LeaseEquipment((Equipment) SearchIdCode.searchItem(itemCode), leasePrice,
									startDate, endDate);
							result.get(invoiceCode).add(equipment);
						}

					} else if (SearchIdCode.searchItem(itemCode).getItemType().equals("P")) {

						Double quantity = Double.parseDouble(token[2]);
						Product product = new Product((Product) SearchIdCode.searchItem(itemCode), quantity);
						result.get(invoiceCode).add(product);

					} else if (SearchIdCode.searchItem(itemCode).getItemType().equals("S")) {

						Double numHours = Double.parseDouble(token[2]);
						Service service = new Service((Service) SearchIdCode.searchItem(itemCode), numHours);
						result.get(invoiceCode).add(service);
					}
				}

			}

		} catch (Exception e) {
			throw new RuntimeException("Encountered Error on line " + line, e);
		}
		return result;
	}

	public static HashMap<String, Invoice> loadInvoice() {
		HashMap<String, Invoice> invoices = loadWithoutItems();
		HashMap<String, List<Item>> invoiceItems = loadInvoiceItems();

		for (String i : invoiceItems.keySet()) {
			for (Item item : invoiceItems.get(i)) {
				invoices.get(i).addInvoiceItem(item);
			}
		}
		return invoices;

	}
}