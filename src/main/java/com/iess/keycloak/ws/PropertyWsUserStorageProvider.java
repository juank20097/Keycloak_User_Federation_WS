package com.iess.keycloak.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iess.keycloak.entities.User;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

public class PropertyWsUserStorageProvider implements 
        UserStorageProvider, 
        UserLookupProvider, 
        CredentialInputValidator,
        CredentialInputUpdater,
        UserRegistrationProvider,
        UserQueryProvider {

    public static String PASSWORD_BASE= "Ecuador2025.";
    
    protected KeycloakSession session;
    protected ComponentModel model;
    protected Map<String, UserModel> loadedUsers = new HashMap<>();

    public PropertyWsUserStorageProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
    	 String API_URL = model.getConfig().getFirst("apiUrl");
    	 UserModel adapter = loadedUsers.get(username);

    	if (adapter == null) {
        // Hacer la petición HTTP al servicio REST
        try {
            String urlString = API_URL + "/" + username + "";
            System.out.println("cadena de username: "+ urlString);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode responseJson = mapper.readTree(conn.getInputStream());

                if (responseJson.isNull() || responseJson.asText().equals("0")) {
                	System.out.println("usuario null");
                    return null;
                }

                String firstName = responseJson.get("first_name").asText();
                String lastName = responseJson.get("last_name").asText();
                String email = responseJson.get("email").asText();
                System.out.println("usuario con datos:"+firstName+lastName+email);
                adapter = createAdapter(realm, username);
                adapter.setFirstName(firstName);
                adapter.setLastName(lastName);
                adapter.setEmail(email);
                loadedUsers.put(username, adapter); // Guardar en caché
                //Guarda en la BDD interna de Keycloak
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    	}
    	
        return adapter;
    }

    protected UserModel createAdapter(RealmModel realm, String username) {
        UserModel user = new AbstractUserAdapterFederatedStorage(session, realm, model) {
            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public void setUsername(String username) {
                // No implementar, ya que el usuario se obtiene desde el WS
            }
        };
        return user;
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        StorageId storageId = new StorageId(id);
        String username = storageId.getExternalId();
        return getUserByUsername(realm, username);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return credentialType.equals(PasswordCredentialModel.TYPE);
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return credentialType.equals(PasswordCredentialModel.TYPE);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
    	String API_URL = model.getConfig().getFirst("apiUrl");
    	if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) return false;
    	UserCredentialModel cred = (UserCredentialModel)input;
    	
    	try {
            String urlString = API_URL + "/" + user.getUsername() + "/"+ cred.getValue();
            System.out.println("cadena de password: "+urlString);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode responseJson = mapper.readTree(conn.getInputStream());

                if (responseJson.asBoolean() == true) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
		return false;
    }

    @Override
    public void close() {
        loadedUsers.clear();
    }

	@Override
	public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult,
			Integer maxResults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult,
			Integer maxResults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserModel getUserByEmail(RealmModel realm, String email) {
		String API_URL = model.getConfig().getFirst("apiUrl");
		 UserModel adapter = null;

	    	if (adapter == null) {
	        // Hacer la petición HTTP al servicio REST
	        try {
	            String urlString = API_URL + "/email/" + email + "";
	            System.out.println("cadena de email: "+ urlString);
	            URL url = new URL(urlString);
	            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	            conn.setRequestMethod("GET");
	            conn.setRequestProperty("Accept", "application/json");

	            if (conn.getResponseCode() == 200) {
	                ObjectMapper mapper = new ObjectMapper();
	                JsonNode responseJson = mapper.readTree(conn.getInputStream());

	                if (responseJson.isNull() || responseJson.asText().equals("0")) {
	                	System.out.println("usuario null");
	                    return null;
	                }

	                String firstName = responseJson.get("first_name").asText();
	                String username = responseJson.get("username").asText();
	                String lastName = responseJson.get("last_name").asText();
	                String email2 = responseJson.get("email").asText();
	                System.out.println("usuario con datos:"+firstName+lastName+username+email2);
	                adapter = createAdapter(realm, username);
	                adapter.setFirstName(firstName);
	                adapter.setLastName(lastName);
	                adapter.setEmail(email2);
	                loadedUsers.put(username, adapter); // Guardar en caché
	                //Guarda en la BDD interna de Keycloak
	                
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    	}
	    	
	        return adapter;
	}
	
	public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
		String API_URL = model.getConfig().getFirst("apiUrl");
	    if (!supportsCredentialType(input.getType())) {
	        throw new RuntimeException("Tipo de credencial no soportado");
	    }

	    if (input instanceof UserCredentialModel) {
	        UserCredentialModel credential = (UserCredentialModel) input;
	        String newPassword = credential.getValue();

	        try {
	            String urlString = API_URL + "/" + user.getUsername() + "/" + newPassword;
	            System.out.println("cadena de change_password: " + urlString);
	            URL url = new URL(urlString);
	            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	            conn.setRequestMethod("PUT");
	            conn.setRequestProperty("Accept", "application/json");

	            // Check the response code
	            int responseCode = conn.getResponseCode();

	            if (responseCode >= 200 && responseCode < 300) { // Successful response (2xx)
	                return true;
	            } else {
	                System.err.println("Error updating credential. Response Code: " + responseCode);
	                return false; // Indicate failure
	            }

	        } catch (IOException e) {
	            e.printStackTrace(); // Print the exception details for debugging
	            return false; // Indicate failure
	        }
	    }
	    return false; // Handle cases where input is not UserCredentialModel
	}

	@Override
	public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserModel addUser(RealmModel realm, String username) {
		MultivaluedMap<String, String> formData = session.getContext().getHttpRequest().getDecodedFormParameters();
	    String firstName = formData.getFirst("firstName");
	    String lastName = formData.getFirst("lastName");
	    String email = formData.getFirst("email");
	    String password = formData.getFirst("password");
	    
	    User u = new User();
	    u.setUsername(username);
	    u.setFirst_name(firstName);
	    u.setLast_name(lastName);
	    u.setEmail(email);
	    u.setPassword(password);
	    
		return null;
	}

	@Override
	public boolean removeUser(RealmModel realm, UserModel user) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
