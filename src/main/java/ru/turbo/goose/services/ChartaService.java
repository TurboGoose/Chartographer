package ru.turbo.goose.services;

import ru.turbo.goose.exceptions.ServiceException;

public interface ChartaService {
    int createCharta(int width, int height) throws ServiceException;
    byte[] getSegment(int id, int x, int y, int w, int h) throws ServiceException;
    void updateSegment(int id, int x, int y, int w, int h, byte[] data) throws ServiceException;
    void deleteCharta(int id) throws ServiceException;
}
