package you.v50to.eatwhat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import you.v50to.eatwhat.data.po.UserInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户信息 Mapper
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}

