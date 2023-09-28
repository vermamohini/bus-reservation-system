package com.tcs.adminms.service;

import static com.tcs.adminms.constants.ErrorConstants.ERR_MSG_ALREADY_EXISTS;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tcs.adminms.entities.BusRoute;
import com.tcs.adminms.exceptions.BusRouteAlreadyExistsException;
import com.tcs.adminms.repository.BusRouteRepository;
import com.tcs.adminms.vo.BusVo;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService implements RabbitListenerConfigurer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminService.class);
	
	private final BusRouteRepository busRouteRepository;
	
	private final RabbitTemplate rabbitTemplate;
	
	@Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
    }
	
	@Value("${spring.rabbitmq.exchange}")
	private String exchange;
	
	@Value("${spring.rabbitmq.routingkey.inventory.create}")
	private String routingKeyInventoryCreate;
	
	public void saveBusRoute(BusRoute route) {
		String busNumber = route.getBusNumber();
		BusRoute existingRoute = busRouteRepository.findById(busNumber).orElse(null);
		if (existingRoute == null) {
			LOGGER.info("Creating new bus route for bus number: {}", busNumber);
			busRouteRepository.save(route);
			
			BusVo busVo = new BusVo();
			busVo.setBusNumber(busNumber);
			busVo.setNoOfSeats(route.getTotalSeats());
			
			LOGGER.info("Inserting event to Exchange: {} with Routing key: {} and message {}:", exchange, routingKeyInventoryCreate, busVo);
			
			rabbitTemplate.convertAndSend(exchange, routingKeyInventoryCreate, busVo);
		}
		else {
			throw new BusRouteAlreadyExistsException(ERR_MSG_ALREADY_EXISTS + route.getBusNumber());
		}	
	}
	
}
