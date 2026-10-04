package com.example.apicustomer.config;

import com.example.apicustomer.entity.Customer;
import com.example.apicustomer.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Configuration
@Profile("!test")
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private static final int NUMBER_OF_CUSTOMERS = 1000;
    
    private static final String[] FIRST_NAMES = {
        "Ana", "Bruno", "Carlos", "Daniela", "Eduardo", "Fernanda", "Gabriel", "Helena",
        "Igor", "Julia", "Lucas", "Mariana", "Nicolas", "Olivia", "Pedro", "Raquel",
        "Samuel", "Tatiana", "Victor", "Yasmin", "Adriano", "Beatriz", "Caio", "Diana",
        "Elias", "Fabiana", "Gustavo", "Isabela", "João", "Larissa", "Mateus", "Natália"
    };
    
    private static final String[] LAST_NAMES = {
        "Silva", "Santos", "Oliveira", "Souza", "Rodrigues", "Ferreira", "Almeida", "Pereira",
        "Lima", "Gomes", "Costa", "Ribeiro", "Martins", "Carvalho", "Araújo", "Melo",
        "Barbosa", "Rocha", "Dias", "Nascimento", "Andrade", "Moreira", "Nunes", "Marques"
    };
    
    private static final String[] CITIES = {
        "São Paulo", "Rio de Janeiro", "Belo Horizonte", "Curitiba", "Porto Alegre",
        "Salvador", "Fortaleza", "Brasília", "Recife", "Manaus", "Goiânia", "Campinas",
        "São Luís", "Maceió", "Natal", "Teresina", "Florianópolis", "João Pessoa"
    };
    
    private static final String[] STREET_TYPES = {
        "Rua", "Avenida", "Alameda", "Travessa", "Praça", "Estrada"
    };

    @Bean
    CommandLineRunner initDatabase(CustomerRepository customerRepository) {
        return args -> {
            long count = customerRepository.count();
            
            if (count == 0) {
                logger.info("Database is empty. Initializing with {} customers...", NUMBER_OF_CUSTOMERS);
                
                List<Customer> customers = generateCustomers();
                customerRepository.saveAll(customers);
                
                logger.info("Successfully created {} customers in the database", NUMBER_OF_CUSTOMERS);
            } else {
                logger.info("Database already contains {} customers. Skipping initialization.", count);
            }
        };
    }
    
    private List<Customer> generateCustomers() {
        List<Customer> customers = new ArrayList<>();
        Set<String> usedEmails = new HashSet<>();
        Random random = new Random();
        
        int attempts = 0;
        int maxAttempts = NUMBER_OF_CUSTOMERS * 10; // Prevent infinite loop
        
        while (customers.size() < NUMBER_OF_CUSTOMERS && attempts < maxAttempts) {
            attempts++;
            
            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            String name = firstName + " " + lastName;
            
            String email = generateEmail(firstName, lastName, customers.size() + 1, random);
            
            // Ensure email is unique
            if (!usedEmails.add(email)) {
                continue; // Skip this iteration if email already exists
            }
            
            String phone = generatePhone(random);
            String address = generateAddress(random);
            
            Customer customer = new Customer(name, email);
            customer.setPhone(phone);
            customer.setAddress(address);
            
            customers.add(customer);
        }
        
        return customers;
    }
    
    private String generateEmail(String firstName, String lastName, int index, Random random) {
        String[] domains = {"gmail.com", "hotmail.com", "outlook.com", "yahoo.com.br", "empresa.com.br"};
        String cleanFirstName = firstName.toLowerCase()
            .replace("á", "a").replace("ã", "a").replace("â", "a")
            .replace("é", "e").replace("ê", "e")
            .replace("í", "i")
            .replace("ó", "o").replace("õ", "o").replace("ô", "o")
            .replace("ú", "u")
            .replace("ç", "c");
        
        String cleanLastName = lastName.toLowerCase()
            .replace("á", "a").replace("ã", "a").replace("â", "a")
            .replace("é", "e").replace("ê", "e")
            .replace("í", "i")
            .replace("ó", "o").replace("õ", "o").replace("ô", "o")
            .replace("ú", "u")
            .replace("ç", "c");
        
        String domain = domains[random.nextInt(domains.length)];
        
        // More variations to ensure better uniqueness
        int variation = random.nextInt(6);
        int randomNumber = random.nextInt(9999);
        
        return switch (variation) {
            case 0 -> cleanFirstName + "." + cleanLastName + "@" + domain;
            case 1 -> cleanFirstName + cleanLastName + index + "@" + domain;
            case 2 -> cleanFirstName.substring(0, 1) + cleanLastName + index + "@" + domain;
            case 3 -> cleanFirstName + "." + cleanLastName + randomNumber + "@" + domain;
            case 4 -> cleanFirstName + cleanLastName + "." + randomNumber + "@" + domain;
            default -> cleanFirstName.substring(0, Math.min(3, cleanFirstName.length())) + 
                       cleanLastName + randomNumber + "@" + domain;
        };
    }
    
    private String generatePhone(Random random) {
        int ddd = 11 + random.nextInt(28); // DDDs do Brasil de 11 a 38
        int part1 = 90000 + random.nextInt(10000); // Números começando com 9
        int part2 = 1000 + random.nextInt(9000);
        return String.format("(%d) %d-%d", ddd, part1, part2);
    }
    
    private String generateAddress(Random random) {
        String streetType = STREET_TYPES[random.nextInt(STREET_TYPES.length)];
        String streetName = LAST_NAMES[random.nextInt(LAST_NAMES.length)] + " " + 
                           FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        int number = random.nextInt(9999) + 1;
        String neighborhood = "Centro";
        String city = CITIES[random.nextInt(CITIES.length)];
        
        return String.format("%s %s, %d - %s, %s", streetType, streetName, number, neighborhood, city);
    }
}
