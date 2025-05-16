package com.chatassist.cozetalk.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// Не импортируем классы с одинаковыми именами, будем использовать полные имена

import com.chatassist.cozetalk.domain.User;
import com.chatassist.cozetalk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Сервис для управления пользователями бота.
 * Обеспечивает регистрацию, поиск и базовые операции с пользователями.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * Поиск пользователя по Telegram ID.
     *
     * @param telegramId ID пользователя в Telegram
     * @return Optional с найденным пользователем или пустой Optional
     */
    @Transactional(readOnly = true)
    public Optional<User> findByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    /**
     * Получение списка всех пользователей.
     *
     * @return Список всех пользователей
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Подсчет общего количества пользователей.
     *
     * @return Количество пользователей
     */
    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }

    /**
     * Регистрация нового пользователя, если он не существует.
     * Если пользователь уже существует, возвращает существующего пользователя.
     *
     * @param telegramUser Объект пользователя из Telegram API
     * @return Созданный или существующий объект пользователя
     */
    @Transactional
    public User registerUserIfNotExists(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        return findByTelegramId(telegramUser.getId())
                .orElseGet(() -> createUser(telegramUser));
    }

    /**
     * Создание нового пользователя на основе данных из Telegram.
     *
     * @param telegramUser Объект пользователя из Telegram API
     * @return Созданный объект пользователя
     */
    @Transactional
    public User createUser(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        User user = new User();
        user.setTelegramId(telegramUser.getId());
        user.setUsername(telegramUser.getUserName());
        user.setFirstName(telegramUser.getFirstName());
        user.setLastName(telegramUser.getLastName());

        log.info("Создан новый пользователь: {}", user);
        return userRepository.save(user);
    }

    /**
     * Обновление данных пользователя.
     *
     * @param user Объект пользователя с обновленными данными
     * @return Обновленный объект пользователя
     */
    @Transactional
    public User updateUser(User user) {
        log.info("Обновлены данные пользователя: {}", user);
        return userRepository.save(user);
    }

    /**
     * Удаление пользователя по Telegram ID.
     *
     * @param telegramId ID пользователя в Telegram
     */
    @Transactional
    public void deleteUserByTelegramId(Long telegramId) {
        userRepository.findByTelegramId(telegramId).ifPresent(user -> {
            userRepository.delete(user);
            log.info("Удален пользователь с Telegram ID: {}", telegramId);
        });
    }

    /**
     * Проверка существования пользователя по Telegram ID.
     *
     * @param telegramId ID пользователя в Telegram
     * @return true, если пользователь существует, иначе false
     */
    @Transactional(readOnly = true)
    public boolean existsByTelegramId(Long telegramId) {
        return userRepository.existsByTelegramId(telegramId);
    }

    /**
     * Получение пользователя по ID в базе данных.
     *
     * @param id ID пользователя в базе данных
     * @return Optional с найденным пользователем или пустой Optional
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}