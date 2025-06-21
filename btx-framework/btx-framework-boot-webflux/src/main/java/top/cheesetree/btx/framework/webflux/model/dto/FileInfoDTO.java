package top.cheesetree.btx.framework.webflux.model.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author van
 * @date 2023/5/25 15:20
 * @description TODO
 */
@Getter
@Setter
public class FileInfoDTO {
    private String filename;
    private String filekey;
    private byte[] filedata;
}
