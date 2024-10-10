package com.example.deliciousBee.service.keyWord;

import org.springframework.stereotype.Service;

import com.example.deliciousBee.repository.KeyWordRepository;
import com.example.deliciousBee.repository.ReviewKeyWordRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeyWordService {
	
	private final KeyWordRepository keyWordRepository;

}
