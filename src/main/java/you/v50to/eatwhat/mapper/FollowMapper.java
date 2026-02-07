package you.v50to.eatwhat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import you.v50to.eatwhat.data.dto.FansDTO;
import you.v50to.eatwhat.data.po.Follow;

import java.util.List;

@Mapper
public interface FollowMapper extends BaseMapper<Follow> {
    
    /**
     * 查询某个用户的粉丝列表
     * @param targetId 被关注者的用户ID
     * @return 粉丝列表
     */
    @Select("""
            SELECT
                u.id,
                u.nick_name AS userName,
                u.avatar,
                CAST(EXTRACT(EPOCH FROM f.created_at) * 1000 AS bigint) AS createdAt
            FROM follow f
            INNER JOIN users u ON f.account_id = u.id
            WHERE f.target_id = #{targetId}
            ORDER BY f.created_at DESC
            """)
    List<FansDTO> selectFollowersByTargetId(@Param("targetId") Long targetId);

    /**
     * 查询某个用户的关注列表
     * @param targetId 关注者的用户ID
     * @return 关注列表
     */
    @Select("""
            SELECT
                u.id,
                u.nick_name AS userName,
                u.avatar,
                CAST(EXTRACT(EPOCH FROM f.created_at) * 1000 AS bigint) AS createdAt
            FROM follow f
            INNER JOIN users u ON f.account_id = u.id
            WHERE f.account_id = #{targetId}
            ORDER BY f.created_at DESC
            """)
    List<FansDTO> selectFollowingsByTargetId(@Param("targetId") Long targetId);
}

