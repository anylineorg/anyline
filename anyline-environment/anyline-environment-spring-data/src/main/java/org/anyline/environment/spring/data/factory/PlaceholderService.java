package org.anyline.environment.spring.data.factory;

import org.anyline.service.init.DefaultService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
@Primary
@Component("anyline.service.default")
public class PlaceholderService extends DefaultService {
}
