package com.sellsphere.admin.mainpage;

import com.sellsphere.admin.article.ArticleService;
import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.ArticleNotFoundException;
import com.sellsphere.common.entity.CarouselImage;
import com.sellsphere.common.entity.payload.CarouselImageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
public class CarouselImageRestController {

    private final CarouselImageService carouselImageService;
    private final ArticleService articleService;

    @PostMapping("/carousel_images/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file,
                                                           @RequestParam("id") Integer id)
            throws ArticleNotFoundException, IOException {
        Article article = articleService.get(id);
        Optional<CarouselImage> carouselImage = carouselImageService.getByArticle(article);
        carouselImage.ifPresent(art -> {
            throw new IllegalArgumentException("Article " + article.getTitle() + " is already assigned to carousel image.");
        });
        carouselImageService.saveImage(file, article);

        Map<String, String> map = new HashMap<>();
        map.put("message", "Successfully uploaded image " + file.getOriginalFilename());
        return ResponseEntity.ok(map);
    }

    @GetMapping("/carousel_images")
    public ResponseEntity<List<CarouselImageDto>> listCarouselImages() {
        List<CarouselImage> carouselImageList = carouselImageService.findAll();

        return ResponseEntity.ok(carouselImageList.stream().map(CarouselImageDto::new).toList());
    }

    @DeleteMapping("/carousel_images/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteCarouselImage(@PathVariable Integer id)
            throws CarouselItemNotFoundException {
        carouselImageService.delete(id);

        Map<String, String> map = new HashMap<>();
        map.put("message", "Successfully deleted image " + id);
        return ResponseEntity.ok(map);
    }

}
