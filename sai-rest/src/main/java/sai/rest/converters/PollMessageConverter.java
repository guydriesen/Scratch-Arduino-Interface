package sai.rest.converters;

import java.io.IOException;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import sai.rest.dto.PollResponse;

public class PollMessageConverter extends AbstractHttpMessageConverter<PollResponse> {
	// http://richardchesterwood.blogspot.nl/2015/02/writing-custom-http-message-converter.html

	public PollMessageConverter() {
		super(MediaType.TEXT_PLAIN);
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return PollResponse.class == clazz;
	}

	@Override
	protected PollResponse readInternal(Class<? extends PollResponse> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		return null;
	}

	@Override
	protected void writeInternal(PollResponse resp, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		outputMessage.getBody().write(resp.toString().getBytes());
	}

}
