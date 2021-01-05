package com.zgyw.materiel.service.impl;

import com.zgyw.materiel.bean.Classify;
import com.zgyw.materiel.bean.MaterielLevel;
import com.zgyw.materiel.bean.MaterielRecords;
import com.zgyw.materiel.repository.MaterielLevelRepository;
import com.zgyw.materiel.repository.MaterielRecordsRepository;
import com.zgyw.materiel.service.ClassifyService;
import com.zgyw.materiel.service.MaterielRecordsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MaterielRecordsServiceImpl implements MaterielRecordsService {
    @Autowired
    private MaterielRecordsRepository repository;
    @Autowired
    private MaterielLevelRepository levelRepository;
    @Autowired
    private ClassifyService classifyService;
    
    @Override
    @Transactional
    public List<MaterielRecords> putInOrder(Integer orderId, String materielIds,Integer type) {
        if (StringUtils.isNotEmpty(materielIds)) {
            Map<Integer, Classify> classifyMap = classifyService.getClassifyIK();
            List<Integer> ids = Arrays.asList(materielIds.split(",")).stream().map(e -> Integer.parseInt(e.trim())).collect(Collectors.toList());
            List<MaterielLevel> materielLevels = levelRepository.findByIdIn(ids);
            List<MaterielRecords> materielRecords = new ArrayList<>();
            for (MaterielLevel materielLevel : materielLevels) {
                MaterielRecords records = new MaterielRecords();
                Classify classify = classifyMap.get(materielLevel.getClassifyId());
                records.setCode(materielLevel.getCode());
                if (classify != null) {
                    records.setName(classify.getName());
                }
                records.setModel(materielLevel.getModel());
                records.setPotting(materielLevel.getPotting());
                records.setBrand(materielLevel.getBrand());
                records.setPrice(materielLevel.getPrice());
                records.setInNum(materielLevel.getQuantity());
                records.setQuantity(materielLevel.getQuantity());
                records.setType(type);
                records.setOrderId(orderId);
                materielRecords.add(records);
            }
            return repository.saveAll(materielRecords);
        }
        return null;
    }

    @Override
    public Map<String,Object> findCurOrder(Integer orderId, String content, Pageable pageable) {
        Map<String, Object> map = new HashMap<>();
        List<MaterielRecords> materielRecords = repository.findByCodeAndOrderIdOrModelAndOrderIdOrPottingAndOrderId(content, orderId, content, orderId, content, orderId, pageable).getContent();
        Integer total = repository.countByCodeAndOrderIdOrModelAndOrderIdOrPottingAndOrderId(content, orderId, content, orderId, content, orderId);
        map.put("materielRecords",materielRecords);
        map.put("total",total);
        return map;
    }

    @Override
    public MaterielRecords detail(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public MaterielRecords modify(MaterielRecords records) {
        MaterielRecords materiel = repository.findById(records.getId()).orElse(null);
        BeanUtils.copyProperties(records,materiel);
        return repository.save(materiel);
    }
}
