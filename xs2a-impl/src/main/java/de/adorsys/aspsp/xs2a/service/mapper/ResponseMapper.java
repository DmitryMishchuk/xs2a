/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.xs2a.service.mapper;

import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ResponseMapper {
//TODO Refactor success operation validation methodology (response.getBody() != null  "bad practice") https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/91
    public ResponseEntity ok(ResponseObject response) {
        return getEntity(response, HttpStatus.OK);
    }

    public ResponseEntity okOrNotFound(ResponseObject response) {
        return getEntity(response, response.getBody() != null
                                   ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    public ResponseEntity createdOrBadRequest(ResponseObject response) {
        return getEntity(response, response.getBody() != null
                                   ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity okOrBadRequest(ResponseObject response) {
        return getEntity(response, response.getBody() != null ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity deleteOrNotFound(ResponseObject response) {
        return getEntity(response, response.getBody() != null
                                   ? HttpStatus.NO_CONTENT : HttpStatus.NOT_FOUND);
    }

    private ResponseEntity getEntity(ResponseObject response, HttpStatus status) {
        MessageError messageError = response.getError();
        return messageError != null
               ? new ResponseEntity<>(messageError, HttpStatus.valueOf(messageError.getTppMessage().getCode().getCode()))
               : new ResponseEntity<>(response.getBody(), status);
    }
}
