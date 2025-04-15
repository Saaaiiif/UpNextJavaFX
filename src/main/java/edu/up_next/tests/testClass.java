package edu.up_next.tests;

import edu.up_next.entities.User;
import edu.up_next.services.UserServices;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class testClass {
    public static void main(String[] args) {
        // Creating roles as a list of strings


        // Create User objects with sample data
        User user = new User(100, "testt@example.com", "ROLE_ARTIST", "password123", "Ness", "Test", "Developer", "A passionate coder", "profile.jpg", true, 123456789, true);
        User user3 = new User(300, "testt@test23.com", "[\"ROLE_CLIENT\"]", "heyy", "heeeeeee", "riahieeeee", "animation", "A passionate coder", "profile.jpg", true, 123456789, true);

        User user2 = new User(220, "testt@test2222.com", "[\"ROLE_ARTIST\"]", "ARTIST", "heeFFFFeeeee", "ARTIST", "animation", "A passionate coder", "profile.jpg", true, 123456789, true);

        User user5 = new User(500, "user5@test.com", "[\"ROLE_CLIENT\"]", "ARTIST", "Test5", "ARTIST", "dancing", "A passionate coder", "profile.jpg", true, 123456789, true);

        // Create the UserServices object
        UserServices us = new UserServices();

        // Add the users to the database
        // us.addUser1(user);
       // us.addUser1(user5);

        System.out.println("User added successfully!");
        System.out.println(us.getAllData());

        // Optional: Test deleting or updating users (if required)
        // us.deleteUser(user3);  // Uncomment this to delete user3


        // Update the user details (e.g., change the email and password)
// New email
      /*  user2.setEmail(
                "updated_email@example.com"
        );

// New password
        user2.setPassword(
                "newPassword123"
        );


// Update the user in the database

        us.updateUser(user2);


        System.out.println(us.getAllData());

*/
   /*     // 🔎 Perform search test
        System.out.println("\n🔍 Searching for users with 'Test':");
        List<User> searchResults = us.searchUsers("Test");

// 🏁 Display search results
        if (searchResults.isEmpty()) {
            System.out.println("❌ No users found!");
        } else {
            for (User foundUser : searchResults) {  // ✅ Renamed 'user' to 'foundUser'
                System.out.println("✅ Found: " + foundUser.getFirstname() + " " + foundUser.getLastname() +
                        " | Email: " + foundUser.getEmail() +
                        " | Roles: " + foundUser.getRoles());
            }
        }


        UserServices userService = new UserServices();
        System.out.println("\n🔍 Test Case 1: Search by Role 'ROLE_ARTIST'");
        List<User> result1 = userService.advancedSearch("ROLE_ARTIST", null, null);
        displayUsers(result1);

        System.out.println("\n🔍 Test Case 2: Search for Active Users");
        List<User> result2 = userService.advancedSearch(null, true, null);
        displayUsers(result2);

        System.out.println("\n🔍 Test Case 3: Search by Keyword 'Ness'");
        List<User> result3 = userService.advancedSearch(null, null, "Ness");
        displayUsers(result3);

        System.out.println("\n🔍 Test Case 4: Search by Role & Keyword ('ROLE_CLIENT', 'Test')");
        List<User> result4 = userService.advancedSearch("ROLE_CLIENT", null, "Test");
        displayUsers(result4);

        System.out.println("\n🔍 Test Case 5: Search by All Criteria ('ROLE_ARTIST', true, 'Ness')");
        List<User> result5 = userService.advancedSearch("ROLE_ARTIST", true, "Ness");
        displayUsers(result5);
    }

    private static void displayUsers(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("❌ No users found!");
        } else {
            for (User user : users) {
                System.out.println("✅ Found: " + user.getFirstname() + " " + user.getLastname() +
                        " | Email: " + user.getEmail() +
                        " | Roles: " + user.getRoles() +
                        " | Active: " + user.isIs_active());
            }
        }
*/

    }

    }

