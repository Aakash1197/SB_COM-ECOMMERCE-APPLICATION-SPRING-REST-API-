package com.ecommerce.project.service;

import com.ecommerce.project.dto.CartDTO;
import com.ecommerce.project.dto.CategoryDTO;
import com.ecommerce.project.dto.ProductDTO;
import com.ecommerce.project.dto.ProductResponse;
import com.ecommerce.project.entity.Cart;
import com.ecommerce.project.entity.Category;
import com.ecommerce.project.entity.Product;
import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.repository.CategoryRepository;
import com.ecommerce.project.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements    ProductService{
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartService cartService;


    @Override
    public ProductDTO addProduct(ProductDTO product, Long categoryId) {


       Category category= categoryRepository.findById(categoryId).orElseThrow(()->
               new ResourceNotFoundException("Category not found","CATEGORYId",categoryId));
       boolean productAvailableOrNot=true;
       List<Product> existingProductList=category.getProducts();
       for(Product products:existingProductList){
            if (products.getProductName().equals(product.getProductName())) {
                productAvailableOrNot = false;
                break;
            }
        }

        if(productAvailableOrNot) {
            Product productEntity = modelMapper.map(product, Product.class);
            productEntity.setCategory(category);
            logger.info("SPECIAL_PRICE VALUE  :" + String.valueOf(product.getPrice() - ((productEntity.getDiscount() * 0.01)) * product.getPrice()));
            productEntity.setSpecial_price(product.getPrice() - ((productEntity.getDiscount() * 0.01)) * product.getPrice());
            productEntity.setProductName(product.getProductName());
            productEntity.setImage(product.getImage());


            return modelMapper.map(productRepository.save(productEntity), ProductDTO.class);
        }
        else{
            throw new APIException("Product already exists!!!");
        }

    }

    @Override
    public ProductResponse getAll(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        logger.info("PageNumber :"+pageNumber +" "+"PageSize :"+ pageSize+"  "+"SortBy  : "+"   "+sortBy  +"SortOrder  :"+sortOrder );
      //List<Product> product=  productRepository.findAll().stream().toList();
        logger.info("PRODUCT DB DATA COUNT : " + productRepository.count());
        if(productRepository.count() == 0 ) {
            throw new APIException("PRODUCT HAS NOT BEEN CREATED TILL NOW!!!");
        }
        logger.info(" SRTING CODE CODE START ");
        Sort sortByAndOrder =
                sortOrder.equalsIgnoreCase("asc")
                        ?(Sort.by(sortBy).ascending()):(Sort.by(sortBy).descending());
        logger.info(" SRTING CODE CODE ENDED ");

        logger.info(" PAGANATION AND SORTING CODE START ");
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> product=  productRepository.findAll(pageDetails);
        logger.info(" 100  "+product.stream().toList());
        List<Product> productPage = product.getContent();
        logger.info(" LIST OF PRODUCT CONTENT IN REQUESTED ORDER  "+productPage.stream().toList());
        logger.info(" PAGANATION AND SORTING CODE END ");

        //ADDED BELOW LINE AS OF NEWER CONVERSION OF PRODUCT TO PRODUCT DTO MODEL
        List<ProductDTO> convertedProductEntityToProductDTO = productPage.stream().map(productEntityMapper ->
                modelMapper.map(productEntityMapper, ProductDTO.class)).toList();

        logger.info("converted product dto :"+convertedProductEntityToProductDTO.stream().toList());





        ProductResponse productResponse=new ProductResponse();
        productResponse.setContent(convertedProductEntityToProductDTO);
        logger.info(" SETTING ALL PAGINATION META DATA STERTED ");
        productResponse.setPageNumber(product.getNumber());
        logger.info(" PAGE NUMBER :" + product.getNumber());
        productResponse.setPageSize(product.getSize());
        logger.info(" PAGE SIZE :" + product.getSize());
        productResponse.setTotalElements(product.getTotalElements());
        logger.info(" TOTAL PAGE ELEMENT :" + product.getTotalElements());
        productResponse.setTotalPages(product.getTotalPages());
        logger.info(" TOTAL PAGE  :" + product.getTotalPages());
        productResponse.setLastPage(product.isLast());
        logger.info(" IS IT LAST PAGE?  :" + product.isLast());
        logger.info(" SETTING ALL PAGINATION META DATA ENDED ");
        //COMMENTED BELOW LINE AS OF OLDER CONVERSION OF CATEGORY TO CATEGORYDTO MODEL
          //  productResponse.setContent(product.stream().map(productStream->modelMapper.map(productStream , ProductDTO.class)).toList());
            logger.info("PRODUCT_RESPONSE  "+productResponse.getContent());
              return  productResponse;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId,Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category= categoryRepository.findById(categoryId).orElseThrow(()->
                new ResourceNotFoundException("Category not found","CATEGORYId",categoryId));
        Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")?
                Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageDetails=PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> pageProduct=productRepository.findByCategoryOrderByPriceAsc(category,pageDetails);

        List<Product> productsEntityList=pageProduct.getContent();

       // List<Product> product=productRepository.findByCategoryOrderByPriceAsc(category);
        if(productsEntityList.isEmpty() ){
            throw new APIException(category.getCategoryName()+"category does not have any products");
        }

        ProductResponse productResponse=new ProductResponse();
        productResponse.setContent(productsEntityList.stream().
                map(productStream->modelMapper.map(productStream ,
                        ProductDTO.class)).toList());
        productResponse.setPageSize(pageProduct.getSize());
        productResponse.setPageNumber(pageProduct.getNumber());
        productResponse.setTotalPages(pageProduct.getTotalPages());
        productResponse.setTotalElements(pageProduct.getTotalElements());
        productResponse.setLastPage(pageProduct.isLast());
        return  productResponse;
    }

    @Override
    public ProductResponse searchProductsByKeyword(String keyword,Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")?
                Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageDetails=PageRequest.of(pageNumber,pageSize,sortByAndOrder);



        Page<Product> pageProduct=productRepository.findByProductNameLikeIgnoreCase('%'+keyword+'%',pageDetails);
        List<Product> productsEntityList=pageProduct.getContent();
        if(productsEntityList.isEmpty() ){
            throw new APIException("Product has not been found with :"+keyword);
        }

        ProductResponse productResponse=new ProductResponse();
        //productResponse.setContent(product.stream().map(productStream->modelMapper.map(productStream , ProductDTO.class)).toList());
        productResponse.setContent(productsEntityList.stream().
                map(productStream->modelMapper.map(productStream ,
                        ProductDTO.class)).toList());
        productResponse.setPageSize(pageProduct.getSize());
        productResponse.setPageNumber(pageProduct.getNumber());
        productResponse.setTotalPages(pageProduct.getTotalPages());
        productResponse.setTotalElements(pageProduct.getTotalElements());
        productResponse.setLastPage(pageProduct.isLast());
        return  productResponse;
    }

    @Override
    public ProductDTO updateProductData(ProductDTO productDTO,Long productId) {
        Product product= productRepository.findById(productId).orElseThrow(()->
                new ResourceNotFoundException("Product not found","Product_id",productId));
        product.setProductName(productDTO.getProductName());
        product.setPrice(productDTO.getPrice());
        product.setDiscount(productDTO.getDiscount());
        product.setDescription(productDTO.getDescription());
        product.setQuantity(productDTO.getQuantity());
        product.setSpecial_price(productDTO.getPrice()-((productDTO.getDiscount()* 0.01))*productDTO.getPrice());

        Product savedProduct =  productRepository.save(product);

        List<Cart> carts = cartRepository.findCartsByProductId(productId);


        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> products = cart.getCartItems().stream()
                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

            cartDTO.setProducts(products);

            return cartDTO;

        }).toList();

        cartDTOs.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(), productId));

        return modelMapper.map(savedProduct, ProductDTO.class);

    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product= productRepository.findById(productId).orElseThrow(()->
                new ResourceNotFoundException("Product not found","Product_id",productId));

        Category category = categoryRepository.findById(product.getCategory().getCategoryId()).orElseThrow(() ->
                new ResourceNotFoundException("Catgory Not found for product_is as", "product_id", productId));
        //DELETE THE CART AS WELL

        List<Cart>  carts=cartRepository.findCartsByProductId(productId);
        carts.forEach(cart-> cartService.deleteProductFromCart(cart.getCartId(),productId));
        productRepository.delete(product);
        return modelMapper.map(product , ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        //get the product from database
        Product product= productRepository.findById(productId).orElseThrow(()->
                new ResourceNotFoundException("Product not found","Product_id",productId));
        //upload the image to server
        //get the file name of uploaded image

        String fileName=fileService.uploadImage(path,image);
        //updating the new file name to the product
        product.setImage(fileName);
        //save the updated product and return DTO after mapping product to DTO
        return modelMapper.map(productRepository.save(product) , ProductDTO.class);
    }



}
