package in.tech_camp.chat_app.repository;
  
  import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import in.tech_camp.chat_app.entity.RoomEntity;
  
  @Mapper
  public interface RoomRepository {
    @Insert("INSERT INTO rooms(room_name) VALUES(#{name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(RoomEntity roomEntity);
  
    @Select("SELECT * FROM rooms WHERE id = #{id}")
    @Results(value = {
        @Result(property = "name", column = "room_name")})
    RoomEntity findById(Integer id);
  }