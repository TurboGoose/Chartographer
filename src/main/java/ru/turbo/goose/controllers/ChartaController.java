package ru.turbo.goose.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.turbo.goose.exceptions.ChartaNotExistsException;
import ru.turbo.goose.exceptions.ImagesDoNotIntersectException;
import ru.turbo.goose.exceptions.ServiceException;
import ru.turbo.goose.exceptions.ValidationException;
import ru.turbo.goose.services.ChartaService;

@RestController
public class ChartaController {
    private final ChartaService service;

    @Autowired
    public ChartaController(ChartaService service) {
        this.service = service;
    }

    @PostMapping("chartas")
    public ResponseEntity<String> create(@RequestParam int width,
                                         @RequestParam int height) {
        try {
            int id = service.createCharta(width, height);
            return ResponseEntity.status(HttpStatus.CREATED).body(Integer.valueOf(id).toString());
        } catch (ServiceException exc) {
            return ResponseEntity.internalServerError().body(exc.getMessage());
        }
    }

    @PostMapping("chartas/{id}")
    public ResponseEntity<Void> update(@PathVariable int id,
                                       @RequestParam int x,
                                       @RequestParam int y,
                                       @RequestParam int width,
                                       @RequestParam int height,
                                       @RequestBody byte[] data) {
        try {
            service.updateSegment(id, x, y, width, height, data);
            return ResponseEntity.ok().build();
        } catch (ValidationException | ImagesDoNotIntersectException exc) {
            return ResponseEntity.badRequest().build();
        } catch (ChartaNotExistsException exc) {
            return ResponseEntity.notFound().build();
        } catch (ServiceException exc) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(path = "chartas/{id}", produces = "image/bmp")
    public ResponseEntity<byte[]> get(@PathVariable int id,
                                           @RequestParam int x,
                                           @RequestParam int y,
                                           @RequestParam int width,
                                           @RequestParam int height) {
        try {
            byte[] data = service.getSegment(id, x, y, width, height);
            return ResponseEntity.ok().body(data);
        } catch (ValidationException | ImagesDoNotIntersectException exc) {
            return ResponseEntity.badRequest().build();
        } catch (ChartaNotExistsException exc) {
            return ResponseEntity.notFound().build();
        } catch (ServiceException exc) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("chartas/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        try {
            service.deleteCharta(id);
            return ResponseEntity.ok().build();
        } catch (ValidationException exc) {
            return ResponseEntity.badRequest().build();
        } catch (ChartaNotExistsException exc) {
            return ResponseEntity.notFound().build();
        } catch (ServiceException exc) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
