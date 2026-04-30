package in.tech_camp.chat_app.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import in.tech_camp.chat_app.entity.UserEntity;

@Mapper
public interface UserRepository {
    @Insert("INSERT INTO users (username, user_email, password) VALUES (#{username}, #{userEmail}, #{password})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(UserEntity user);  

    @Select("SELECT * FROM users WHERE user_email = #{userEmail}")
    UserEntity findByEmail(String userEmail); 
    
    @Select("SELECT * FROM users WHERE id = #{id}")
    UserEntity findById(Integer id);

    @Update("UPDATE users SET username = #{username}, user_email = #{userEmail} WHERE id = #{id}")
    void update(UserEntity user);
}
