package in.tech_camp.chat_app.factories;

import com.github.javafaker.Faker;

import in.tech_camp.chat_app.form.UserEditForm;

public class UserEditFormFactory {
  private static final Faker faker = new Faker();

  public static UserEditForm createEditUser() {
    UserEditForm userEditForm = new UserEditForm();

    userEditForm.setUsername(faker.name().username());
    userEditForm.setUserEmail(faker.internet().emailAddress());

    return userEditForm;
  }
}